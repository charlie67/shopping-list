import {createAsyncThunk, createSlice, type PayloadAction} from '@reduxjs/toolkit';
import type {
    FrozenMealDto,
    PlanBatchDto,
    PlanCreateDto,
    PlannedMealCreateDto,
    PlannedMealUpdateDto,
    PlanWeekDto,
} from '@/common/types/plan';
import * as api from '@/common/api/plan.api';
import * as optionsApi from '@/common/api/options.api';
import type {RootState} from '@/store/store';

const DEFAULT_HOUSEHOLD = 2;
const MIN_HOUSEHOLD = 1;
const MAX_HOUSEHOLD = 8;

/**
 * The option is free-form text on the wire and another client could legitimately have written
 * anything into it, so it is parsed defensively at both ends.
 */
export function parseHousehold(value: string): number {
    const parsed = Number.parseInt(value, 10);
    if (!Number.isFinite(parsed)) return DEFAULT_HOUSEHOLD;
    return Math.min(MAX_HOUSEHOLD, Math.max(MIN_HOUSEHOLD, parsed));
}

interface PlanState {
    // Batches keyed by the ISO date of their week's Monday.
    weeks: Record<string, PlanBatchDto[]>;
    // Global rather than week-scoped, so every week response carries the current truth for both.
    queue: PlanBatchDto[];
    freezer: FrozenMealDto[];
    household: number;
    fetchStatus: Record<string, 'idle' | 'loading' | 'failed'>;
    planStatus: 'idle' | 'loading' | 'failed';
    mutateStatus: 'idle' | 'loading' | 'failed';
    householdStatus: 'idle' | 'loading' | 'failed';
    // When this client last wrote, so it can ignore the echo of its own broadcast.
    lastMutationAt: number;
}

const initialState: PlanState = {
    weeks: {},
    queue: [],
    freezer: [],
    household: DEFAULT_HOUSEHOLD,
    fetchStatus: {},
    planStatus: 'idle',
    mutateStatus: 'idle',
    householdStatus: 'idle',
    lastMutationAt: 0,
};

/**
 * Stores a week the server just sent. After a mutation any *other* cached week may be stale — a
 * meal can be moved out of the week you are looking at — so those are dropped and refetched when
 * they are next visited.
 */
function applyWeek(state: PlanState, week: PlanWeekDto, invalidateOthers = false) {
    if (invalidateOthers) {
        state.weeks = {};
    }
    state.weeks[week.weekStart] = week.batches;
    state.queue = week.queue;
    state.freezer = week.freezer;
    state.household = week.cookingFor;
    state.fetchStatus[week.weekStart] = 'idle';
}

export const fetchPlanWeek = createAsyncThunk(
    'plan/fetchWeek',
    async (weekStart: string) => {
        return api.getPlanWeek(weekStart);
    },
);

// Only needed by the plan sheet opened from the Recipes page, which has no week loaded. The To Cook
// page gets the household from the week response instead, so booting it costs one request.
export const fetchHousehold = createAsyncThunk(
    'plan/fetchHousehold',
    async () => {
        return optionsApi.getOption('PLAN_PORTION_SIZE');
    },
);

export const setHousehold = createAsyncThunk(
    'plan/setHousehold',
    async ({people, weekStart}: { people: number; weekStart?: string }, thunkApi) => {
        const option = await optionsApi.updateOption('PLAN_PORTION_SIZE', String(people));
        // The backend reshapes this week and later to the new household, so whatever is on screen
        // has to be read again.
        if (weekStart) {
            thunkApi.dispatch(fetchPlanWeek(weekStart));
        }
        return option;
    },
);

export const planRecipe = createAsyncThunk(
    'plan/planRecipe',
    async ({plan, weekStart}: { plan: PlanCreateDto; weekStart: string }) => {
        void weekStart;
        return api.planRecipe(plan);
    },
);

// Every mutation below rolls back by refetching rather than by restoring a snapshot: after a
// cascade — a moved cook night stranding its leftovers, say — only the server knows what the week
// should look like, and rebuilding that in a reducer is bookkeeping that would rot.
const refetchOnFailure = async <T>(
    work: () => Promise<T>,
    weekStart: string,
    dispatch: (action: unknown) => unknown,
): Promise<T> => {
    try {
        return await work();
    } catch (error) {
        dispatch(fetchPlanWeek(weekStart));
        throw error;
    }
};

export const placeMeal = createAsyncThunk(
    'plan/placeMeal',
    async (
        {batchId, meal, weekStart}:
            { batchId: string; meal: PlannedMealCreateDto; weekStart: string },
        thunkApi,
    ) => {
        return refetchOnFailure(() => api.placeMeal(batchId, meal, weekStart), weekStart, thunkApi.dispatch);
    },
);

export const moveMeal = createAsyncThunk(
    'plan/moveMeal',
    async (
        {mealId, update, weekStart}:
            { mealId: string; update: PlannedMealUpdateDto; weekStart: string },
        thunkApi,
    ) => {
        return refetchOnFailure(() => api.updateMeal(mealId, update, weekStart), weekStart, thunkApi.dispatch);
    },
);

export const unplaceMeal = createAsyncThunk(
    'plan/unplaceMeal',
    async ({mealId, weekStart}: { mealId: string; weekStart: string }, thunkApi) => {
        return refetchOnFailure(() => api.unplaceMeal(mealId, weekStart), weekStart, thunkApi.dispatch);
    },
);

export const removeBatch = createAsyncThunk(
    'plan/removeBatch',
    async ({batchId, weekStart}: { batchId: string; weekStart: string }, thunkApi) => {
        return refetchOnFailure(() => api.removeBatch(batchId, weekStart), weekStart, thunkApi.dispatch);
    },
);

/** Finds a meal across the cached weeks and the queue, for the optimistic edits below. */
function findMeal(state: PlanState, mealId: string) {
    const batches = [...Object.values(state.weeks).flat(), ...state.queue];
    for (const batch of batches) {
        const meal = batch.meals.find((candidate) => candidate.id === mealId);
        if (meal) return {batch, meal};
    }
    return undefined;
}

const planSlice = createSlice({
    name: 'plan',
    initialState,
    reducers: {
        // Dispatched by the websocket layer when another device changed a week we may be showing.
        planWeeksInvalidated(state, action: PayloadAction<{ weekStarts: string[] }>) {
            for (const weekStart of action.payload.weekStarts) {
                delete state.weeks[weekStart];
            }
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchPlanWeek.pending, (state, action) => {
                state.fetchStatus[action.meta.arg] = 'loading';
            })
            .addCase(fetchPlanWeek.fulfilled, (state, action) => {
                applyWeek(state, action.payload);
            })
            .addCase(fetchPlanWeek.rejected, (state, action) => {
                state.fetchStatus[action.meta.arg] = 'failed';
            })

            .addCase(fetchHousehold.fulfilled, (state, action) => {
                state.household = parseHousehold(action.payload.value);
                state.householdStatus = 'idle';
            })
            .addCase(fetchHousehold.rejected, (state) => {
                state.householdStatus = 'failed';
            })

            // The stepper has to feel instant, so the number moves before the request resolves.
            .addCase(setHousehold.pending, (state, action) => {
                state.household = action.meta.arg.people;
                state.householdStatus = 'loading';
            })
            .addCase(setHousehold.fulfilled, (state, action) => {
                state.household = parseHousehold(action.payload.value);
                state.householdStatus = 'idle';
                state.lastMutationAt = Date.now();
            })
            .addCase(setHousehold.rejected, (state) => {
                state.householdStatus = 'failed';
            })

            // Not optimistic: the sheet stays open until the server confirms, so a failure leaves
            // the form intact to retry rather than a plan that never happened.
            .addCase(planRecipe.pending, (state) => {
                state.planStatus = 'loading';
            })
            .addCase(planRecipe.fulfilled, (state, action) => {
                state.planStatus = 'idle';
                applyWeek(state, action.payload, true);
                state.lastMutationAt = Date.now();
            })
            .addCase(planRecipe.rejected, (state) => {
                state.planStatus = 'failed';
            })

            // Optimistic: a dragged chip must land under the finger, not a round trip later.
            .addCase(moveMeal.pending, (state, action) => {
                state.mutateStatus = 'loading';
                const found = findMeal(state, action.meta.arg.mealId);
                if (found) {
                    found.meal.placement = action.meta.arg.update.placement;
                    found.meal.plannedDate = action.meta.arg.update.plannedDate;
                }
            })
            // Optimistic too: the chip disappears as it is dropped on the pool.
            .addCase(unplaceMeal.pending, (state, action) => {
                state.mutateStatus = 'loading';
                const found = findMeal(state, action.meta.arg.mealId);
                if (found) {
                    found.batch.meals = found.batch.meals.filter(
                        (meal) => meal.id !== action.meta.arg.mealId);
                    found.batch.spareMeals += 1;
                }
            })
            .addMatcher(
                (action) => [placeMeal.pending.type, removeBatch.pending.type].includes(action.type),
                (state) => {
                    state.mutateStatus = 'loading';
                },
            )
            .addMatcher(
                (action) => [
                    placeMeal.fulfilled.type,
                    moveMeal.fulfilled.type,
                    unplaceMeal.fulfilled.type,
                    removeBatch.fulfilled.type,
                ].includes(action.type),
                (state, action: PayloadAction<PlanWeekDto>) => {
                    state.mutateStatus = 'idle';
                    applyWeek(state, action.payload, true);
                    state.lastMutationAt = Date.now();
                },
            )
            .addMatcher(
                (action) => [
                    placeMeal.rejected.type,
                    moveMeal.rejected.type,
                    unplaceMeal.rejected.type,
                    removeBatch.rejected.type,
                ].includes(action.type),
                (state) => {
                    // The refetch dispatched inside the thunk puts the week back.
                    state.mutateStatus = 'failed';
                },
            );
    },
});

export const {planWeeksInvalidated} = planSlice.actions;

export const selectPlanWeekBatches = (weekStart: string) =>
    (state: RootState): PlanBatchDto[] | undefined => state.plan.weeks[weekStart];
export const selectPlanWeekStatus = (weekStart: string) =>
    (state: RootState) => state.plan.fetchStatus[weekStart] ?? 'idle';
export const selectPlanQueue = (state: RootState) => state.plan.queue;
export const selectPlanFreezer = (state: RootState) => state.plan.freezer;
export const selectHousehold = (state: RootState) => state.plan.household;
export const selectPlanStatus = (state: RootState) => state.plan.planStatus;
export const selectPlanMutateStatus = (state: RootState) => state.plan.mutateStatus;
export const selectPlanLastMutationAt = (state: RootState) => state.plan.lastMutationAt;

export default planSlice.reducer;
