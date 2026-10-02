import {useCallback, useEffect, useState} from 'react';
import {Loader2} from 'lucide-react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useRecipeById} from '@/common/hooks/useRecipeById';
import {fromIsoDate, toIsoDate, weekDays} from '@/common/date/week';
import {useSelectedRecipeId} from '@/features/recipes/useSelectedRecipeId';
import {RecipeDetailModal} from '@/features/recipes/components/RecipeDetailModal';
import type {FrozenMealDto, MealPlacement, PlanBatchDto} from '@/common/types/plan';
import {
    fetchPlanWeek,
    moveMeal,
    placeMeal,
    removeBatch,
    selectHousehold,
    selectPlanFreezer,
    selectPlanQueue,
    selectPlanWeekBatches,
    selectPlanWeekStatus,
    setHousehold,
    unplaceMeal,
} from './planSlice';
import {isQueued} from './portions';
import {PlannerDragProvider} from './dnd/PlannerDragContext';
import {type DragPayload, type DropTarget, resolveDrop} from './dnd/plannerDnd';
import {PlacementSheet, type PlacementTarget} from './components/PlacementSheet';
import {useWeekStart} from './useWeekStart';
import {CoverageBar} from './components/CoverageBar';
import {FreezerPanel} from './components/FreezerPanel';
import {HouseholdStepper} from './components/HouseholdStepper';
import {PlannedBatchRow} from './components/PlannedBatchRow';
import {WeekRibbon} from './components/WeekRibbon';
import {WeekSwitcher} from './components/WeekSwitcher';

export function ToCookPage() {
    const dispatch = useAppDispatch();
    const [weekStart, setWeekStart] = useWeekStart();
    const [selectedRecipeId, setSelectedRecipeId] = useSelectedRecipeId();

    const batches = useAppSelector(selectPlanWeekBatches(weekStart));
    const status = useAppSelector(selectPlanWeekStatus(weekStart));
    const queue = useAppSelector(selectPlanQueue);
    const freezer = useAppSelector(selectPlanFreezer);
    const household = useAppSelector(selectHousehold);
    const selectedRecipe = useRecipeById(selectedRecipeId);
    // Which meal the placement sheet is deciding about, if any.
    const [placing, setPlacing] = useState<PlacementTarget | null>(null);
    // Why the last drop was refused, shown briefly rather than swallowed in silence.
    const [refusal, setRefusal] = useState<string | null>(null);

    useEffect(() => {
        if (batches === undefined && status !== 'loading' && status !== 'failed') {
            dispatch(fetchPlanWeek(weekStart));
        }
    }, [dispatch, weekStart, batches, status]);

    // Another phone in the kitchen may have moved something, and a phone that has been asleep since
    // yesterday is showing the wrong "today". Both are fixed by reading the week again on return.
    useEffect(() => {
        const onVisible = () => {
            if (document.visibilityState === 'visible') {
                dispatch(fetchPlanWeek(weekStart));
            }
        };
        document.addEventListener('visibilitychange', onVisible);
        return () => document.removeEventListener('visibilitychange', onVisible);
    }, [dispatch, weekStart]);

    const changeHousehold = useCallback((people: number) => {
        dispatch(setHousehold({people, weekStart}));
    }, [dispatch, weekStart]);

    const place = (placement: MealPlacement, plannedDate: string | null) => {
        if (!placing) return;
        if (placing.kind === 'spare') {
            dispatch(placeMeal({batchId: placing.batch.id, meal: {placement, plannedDate}, weekStart}));
        } else {
            dispatch(moveMeal({mealId: placing.mealId, update: {placement, plannedDate}, weekStart}));
        }
        setPlacing(null);
    };

    const unplace = (mealId: string) => {
        dispatch(unplaceMeal({mealId, weekStart}));
        setPlacing(null);
    };

    const openMeal = (batch: PlanBatchDto, mealId: string) => {
        const meal = batch.meals.find((candidate) => candidate.id === mealId);
        if (meal) {
            setPlacing({kind: 'meal', mealId, placement: meal.placement, batch});
        }
    };

    const useFrozenMeal = (meal: FrozenMealDto) => setPlacing({
        kind: 'frozen',
        mealId: meal.id,
        recipeId: meal.recipeId,
        recipeName: meal.recipeName,
    });

    // Drag is only ever a shortcut: it decides what the drop means and then dispatches exactly the
    // thunks the placement sheet uses.
    const handleDrop = (payload: DragPayload, target: DropTarget) => {
        const outcome = resolveDrop(payload, target);
        switch (outcome.action) {
            case 'place':
                dispatch(placeMeal({
                    batchId: outcome.batchId,
                    meal: {placement: outcome.placement, plannedDate: outcome.plannedDate},
                    weekStart,
                }));
                break;
            case 'move':
                dispatch(moveMeal({
                    mealId: outcome.mealId,
                    update: {placement: outcome.placement, plannedDate: outcome.plannedDate},
                    weekStart,
                }));
                break;
            case 'unplace':
                dispatch(unplaceMeal({mealId: outcome.mealId, weekStart}));
                break;
            case 'refuse':
                setRefusal(outcome.reason);
                window.setTimeout(() => setRefusal(null), 3500);
                break;
        }
    };

    // What a batch of this recipe was planned for, when that differs from the recipe's own yield.
    // The queue counts too, since a queued batch is just one with no night yet.
    const plannedServingsOf = (recipeId: string) => {
        const batch = [...(batches ?? []), ...queue].find(
            (candidate) => candidate.recipeId === recipeId);
        return batch === undefined || batch.cookingFor === batch.servings
            ? null
            : batch.cookingFor;
    };

    const isoDates = weekDays(fromIsoDate(weekStart)).map(toIsoDate);
    const weekBatches = batches ?? [];
    const planned = weekBatches.filter((batch) => !isQueued(batch));

    return (
        <div className="mx-auto w-full max-w-6xl px-4 py-6">
            <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold tracking-tight text-white">To Cook</h1>
                    <p className="mt-1 text-sm text-gray-400">
                        {planned.length} {planned.length === 1 ? 'recipe' : 'recipes'} this week
                    </p>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    <WeekSwitcher weekStart={weekStart} onChange={setWeekStart}/>
                    <HouseholdStepper household={household} onChange={changeHousehold}/>
                </div>
            </div>

            {status === 'failed' ? (
                <div className="rounded-2xl bg-gray-900 p-8 text-center ring-1 ring-white/5">
                    <p className="text-sm text-gray-400">Could not load this week.</p>
                    <button
                        type="button"
                        onClick={() => dispatch(fetchPlanWeek(weekStart))}
                        className="mt-3 cursor-pointer rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-500"
                    >
                        Try again
                    </button>
                </div>
            ) : (
                <PlannerDragProvider onDrop={handleDrop}>
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_20rem]">
                    <div className="flex flex-col gap-4">
                        <section className="rounded-2xl bg-gray-900 ring-1 ring-white/5">
                            <div className="px-4 pb-3 pt-4">
                                <h2 className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                                    Your week
                                </h2>
                            </div>
                            <WeekRibbon
                                isoDates={isoDates}
                                batches={weekBatches}
                                onSelectMeal={openMeal}
                            />
                            <CoverageBar
                                batches={weekBatches}
                                isoDates={isoDates}
                                frozenCount={freezer.length}
                            />
                        </section>

                        <section className="rounded-2xl bg-gray-900 ring-1 ring-white/5">
                            <div className="px-4 pb-3 pt-4">
                                <h2 className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                                    Planned
                                </h2>
                            </div>
                            {status === 'loading' && batches === undefined ? (
                                <div className="flex justify-center py-8">
                                    <Loader2 size={22} className="animate-spin text-gray-500"/>
                                </div>
                            ) : planned.length === 0 ? (
                                <p className="px-4 pb-5 text-sm leading-relaxed text-gray-600">
                                    Nothing planned for this week yet. Plan a recipe from the
                                    Recipes tab.
                                </p>
                            ) : (
                                <div className="flex flex-col gap-1.5 px-2.5 pb-3">
                                    {planned.map((batch) => (
                                        <PlannedBatchRow
                                            key={batch.id}
                                            batch={batch}
                                            household={household}
                                            onOpenRecipe={setSelectedRecipeId}
                                            onOpenMeal={openMeal}
                                            onUnplaceMeal={unplace}
                                            onPlaceSpare={(target) => setPlacing({kind: 'spare', batch: target})}
                                            onRemoveBatch={(batchId) =>
                                                dispatch(removeBatch({batchId, weekStart}))}
                                        />
                                    ))}
                                </div>
                            )}

                            {queue.length > 0 && (
                                <>
                                    <div className="mx-4 mb-2.5 mt-3 flex items-center gap-2.5">
                                        <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-600">
                                            Queue · no day yet
                                        </span>
                                        <span className="h-px flex-1 bg-white/5"/>
                                    </div>
                                    <div className="flex flex-col gap-1.5 px-2.5 pb-3">
                                        {queue.map((batch) => (
                                            <PlannedBatchRow
                                                key={batch.id}
                                                batch={batch}
                                                household={household}
                                                onOpenRecipe={setSelectedRecipeId}
                                                onPlaceSpare={(target) =>
                                                    setPlacing({kind: 'spare', batch: target})}
                                                onRemoveBatch={(batchId) =>
                                                    dispatch(removeBatch({batchId, weekStart}))}
                                            />
                                        ))}
                                    </div>
                                </>
                            )}
                        </section>
                    </div>

                    <FreezerPanel
                        freezer={freezer}
                        onOpenRecipe={setSelectedRecipeId}
                        onUseMeal={useFrozenMeal}
                        onUnfreeze={unplace}
                    />
                </div>
                </PlannerDragProvider>
            )}

            {refusal && (
                <div
                    role="status"
                    className="fixed inset-x-4 bottom-6 z-50 mx-auto max-w-sm rounded-xl bg-gray-900 px-4 py-3 text-center text-sm text-gray-200 shadow-lg shadow-black/50 ring-1 ring-white/15"
                >
                    {refusal}
                </div>
            )}

            {placing && (
                <PlacementSheet
                    target={placing}
                    weekStart={weekStart}
                    batches={weekBatches}
                    onPlace={place}
                    onUnplace={placing.kind === 'spare' ? undefined : () => unplace(placing.mealId)}
                    onOpenRecipe={(recipeId) => {
                        setPlacing(null);
                        setSelectedRecipeId(recipeId);
                    }}
                    onClose={() => setPlacing(null)}
                />
            )}

            {selectedRecipe && (
                <RecipeDetailModal
                    recipe={selectedRecipe}
                    // Opened from a planned batch, the portions on show are the ones that batch was
                    // planned for, not the recipe's own yield.
                    plannedServings={plannedServingsOf(selectedRecipe.id)}
                    onClose={() => setSelectedRecipeId(null)}
                />
            )}
        </div>
    );
}
