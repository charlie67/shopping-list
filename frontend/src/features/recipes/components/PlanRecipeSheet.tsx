import {useEffect, useMemo, useState} from 'react';
import {Check, ChevronLeft, ChevronRight, Minus, Plus, X} from 'lucide-react';
import type {ExtractedRecipeDto} from '@/common/types/recipe';
import type {PlanLeftoverRequest} from '@/common/types/plan';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {addShoppingListItems} from '@/features/shopping-list/shoppingListSlice';
import {
    fetchHousehold,
    fetchPlanWeek,
    planRecipe,
    selectHousehold,
    selectPlanStatus,
    selectPlanWeekBatches,
} from '@/features/plan/planSlice';
import {mealsForDay} from '@/features/plan/portions';
import {defaultLeftovers, type LeftoverChoice} from '@/features/plan/leftovers';
import {
    addDays,
    addWeeks,
    dayName,
    formatDayChip,
    formatWeekRange,
    fromIsoDate,
    startOfWeek,
    toIsoDate,
    weekDays,
    weekLabel,
} from '@/common/date/week';
import {useIngredientSelection} from '../useIngredientSelection';
import {isScaled} from '../scaleIngredient';
import {IngredientChecklist} from './IngredientChecklist';

interface Props {
    recipe: ExtractedRecipeDto;
    onClose: () => void;
    onOpenPlanner?: () => void;
}

const QUEUE = 'queue';

export function PlanRecipeSheet({recipe, onClose, onOpenPlanner}: Props) {
    const dispatch = useAppDispatch();
    const household = useAppSelector(selectHousehold);
    const planStatus = useAppSelector(selectPlanStatus);

    const [cookDate, setCookDate] = useState<string | typeof QUEUE>(() =>
        toIsoDate(new Date()));
    // The week whose nights are on offer, held separately from the chosen one so you can look ahead
    // at what is already planned before committing this recipe to a night in it.
    const [viewWeek, setViewWeek] = useState(() => toIsoDate(startOfWeek(new Date())));
    // Tracks the household setting until the user sets a number for this batch themselves. Holding
    // it in state directly would capture whatever the store had at mount - the default of 2 when the
    // sheet is opened from the Recipes page - and never catch up once the real value arrived.
    const [peopleOverride, setPeopleOverride] = useState<number | null>(null);
    const people = peopleOverride ?? household;
    // How many this batch is being cooked for, when that is not the number the recipe was written
    // for. Scales the quantities onto the shopping list and the meal count of the batch; the stored
    // recipe keeps its own yield either way.
    const [servesOverride, setServesOverride] = useState<number | null>(null);
    // Only used when the recipe's yield could not be read, where we ask instead of guessing.
    const [manualMeals, setManualMeals] = useState(1);

    // How many this batch feeds, and what that does to the recipe's own amounts. A recipe with no
    // readable yield has nothing to scale from, so it stays at 1 and the meal count is typed in.
    const serves = servesOverride ?? recipe.servings ?? 1;
    const scale = recipe.servings === null || recipe.servings <= 0 ? 1 : serves / recipe.servings;
    const selection = useIngredientSelection(recipe.ingredients, scale);
    const [leftovers, setLeftovers] = useState<LeftoverChoice[]>([]);
    const [ingredientsOpen, setIngredientsOpen] = useState(false);
    const [planned, setPlanned] = useState<{ itemsAdded: number } | null>(null);

    useEscapeKey(onClose);

    // Opened from the Recipes page the store holds nothing but the default household, and the
    // portions maths is built on it, so read it rather than waiting on a week response that may be
    // slow, may fail, or - for an already cached week - never be requested at all.
    useEffect(() => {
        dispatch(fetchHousehold());
    }, [dispatch]);

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    // Leftovers are placed relative to the cook night, so they stay anchored to its week even after
    // you have navigated the view somewhere else.
    const cookWeek = cookDate === QUEUE
        ? viewWeek
        : toIsoDate(startOfWeek(fromIsoDate(cookDate)));

    const viewBatches = useAppSelector(selectPlanWeekBatches(viewWeek));
    const cookBatches = useAppSelector(selectPlanWeekBatches(cookWeek));

    // The sheet can be opened from the Recipes page, which has no week loaded, and it needs one to
    // show what is already on each night and to pick sensible nights for the leftovers. The two
    // weeks are the same until you navigate away from the one you are cooking in.
    useEffect(() => {
        if (viewBatches === undefined) {
            dispatch(fetchPlanWeek(viewWeek));
        }
    }, [dispatch, viewWeek, viewBatches]);

    useEffect(() => {
        if (cookBatches === undefined) {
            dispatch(fetchPlanWeek(cookWeek));
        }
    }, [dispatch, cookWeek, cookBatches]);

    const toDays = (weekStart: string) => weekDays(fromIsoDate(weekStart)).map((date) => ({
        iso: toIsoDate(date),
        name: dayName(date),
        number: date.getDate(),
    }));

    const days = useMemo(() => toDays(viewWeek), [viewWeek]);
    const cookWeekDays = useMemo(() => toDays(cookWeek), [cookWeek]);

    const occupantsIn = (batches: typeof viewBatches, iso: string) =>
        (batches ?? []).length === 0 ? [] : mealsForDay(batches ?? [], iso);

    const occupants = (iso: string) => occupantsIn(viewBatches, iso);

    const mealsTotal = recipe.servings === null
        ? manualMeals
        : Math.max(1, Math.round(serves / people));
    const leftoverCount = Math.max(0, mealsTotal - 1);

    useEffect(() => {
        setLeftovers((previous) => defaultLeftovers({
            previous,
            leftoverCount,
            cookDate: cookDate === QUEUE ? null : cookDate,
            isFree: (iso) => occupantsIn(cookBatches, iso).length === 0,
            weekDays: cookWeekDays.map((day) => day.iso),
            addDays: (iso, offset) => toIsoDate(addDays(fromIsoDate(iso), offset)),
        }));
        // Re-running on every batches change would fight the user's own choices, so this follows
        // the count and the cook night only.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [leftoverCount, cookDate]);

    const setLeftover = (index: number, choice: LeftoverChoice) => {
        setLeftovers((previous) => previous.map((item, i) => (i === index ? choice : item)));
    };

    const placedLeftovers = leftovers.filter((l) => l.mode === 'day').length;
    const frozen = leftovers.filter((l) => l.mode === 'freeze').length;
    const titles = selection.titles();

    const confirm = async () => {
        const leftoverRequests = leftovers.flatMap<PlanLeftoverRequest>((choice) => {
            if (choice.mode === 'day') {
                return [{placement: 'LEFTOVER', plannedDate: choice.plannedDate}];
            }
            if (choice.mode === 'freeze') return [{placement: 'FROZEN'}];
            return [];
        });

        try {
            await dispatch(planRecipe({
                plan: {
                    recipeId: recipe.id,
                    cookDate: cookDate === QUEUE ? null : cookDate,
                    cookingFor: people,
                    // Sent explicitly whenever the server could not work it out itself, or would
                    // work it out from the recipe's own yield and miss the scaling. The batch holds
                    // the scaled count; the recipe row is untouched.
                    ...(recipe.servings === null || isScaled(scale) ? {mealsTotal} : {}),
                    leftovers: cookDate === QUEUE ? [] : leftoverRequests,
                },
                weekStart: cookWeek,
            })).unwrap();
        } catch {
            // planStatus carries the failure; the sheet stays open with everything still filled in.
            return;
        }

        // A separate call on purpose: a failure here leaves the plan standing and only the shopping
        // half to retry, rather than an ambiguous half-done write.
        if (titles.length > 0) {
            dispatch(addShoppingListItems(titles));
        }
        setPlanned({itemsAdded: titles.length});
    };

    const sectionTitle = 'text-[11px] font-semibold uppercase tracking-wider text-gray-400';

    return (
        <div
            // cursor-default because `cursor` is inherited: this sheet is a DOM child of whatever
            // opened it, and a RecipeCard is itself one big cursor-pointer button, so without this
            // every inch of the sheet claims to be clickable.
            className="fixed inset-0 z-50 flex h-[100dvh] cursor-default items-stretch justify-center bg-black/70 backdrop-blur-sm sm:items-center sm:p-4"
            onClick={onClose}
            role="dialog"
            aria-modal="true"
            aria-label={`Plan ${recipe.name}`}
        >
            <div
                className="relative flex h-full w-full flex-col overflow-hidden bg-gray-900 ring-1 ring-white/10 sm:h-auto sm:max-h-[88vh] sm:max-w-lg sm:rounded-2xl"
                onClick={(e) => e.stopPropagation()}
            >
                {planned ? (
                    <PlannedRecap
                        recipe={recipe}
                        cookDate={cookDate === QUEUE ? null : cookDate}
                        placedLeftovers={placedLeftovers}
                        frozen={frozen}
                        itemsAdded={planned.itemsAdded}
                        onClose={onClose}
                        onOpenPlanner={onOpenPlanner}
                    />
                ) : (
                    <>
                        <div className="flex items-start justify-between gap-3 border-b border-white/5 p-4">
                            <div className="min-w-0">
                                <h2 className="truncate text-sm font-semibold text-white">Plan to cook</h2>
                                <p className="truncate text-xs text-gray-400">
                                    {recipe.name}
                                    {recipe.servings !== null && ` · serves ${recipe.servings}`}
                                </p>
                            </div>
                            <button
                                onClick={onClose}
                                aria-label="Close"
                                className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full bg-white/5 text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                            >
                                <X size={16}/>
                            </button>
                        </div>

                        <div className="flex-1 overflow-y-auto">
                            {/* ---------------------------------------------------------- when */}
                            <section className="border-b border-white/5 p-4">
                                <div className="flex items-center justify-between gap-2">
                                    <h3 className={sectionTitle}>When</h3>
                                    <div className="flex items-center gap-1">
                                        <button
                                            type="button"
                                            onClick={() => setViewWeek(toIsoDate(addWeeks(fromIsoDate(viewWeek), -1)))}
                                            aria-label="Previous week"
                                            className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-white/10 hover:text-white"
                                        >
                                            <ChevronLeft size={16}/>
                                        </button>
                                        <span className="min-w-[8.5rem] text-center">
                                            <span className="block text-xs font-semibold text-white">
                                                {weekLabel(fromIsoDate(viewWeek), new Date())}
                                            </span>
                                            <span className="block text-[10px] text-gray-500">
                                                {formatWeekRange(fromIsoDate(viewWeek))}
                                            </span>
                                        </span>
                                        <button
                                            type="button"
                                            onClick={() => setViewWeek(toIsoDate(addWeeks(fromIsoDate(viewWeek), 1)))}
                                            aria-label="Next week"
                                            className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-white/10 hover:text-white"
                                        >
                                            <ChevronRight size={16}/>
                                        </button>
                                    </div>
                                </div>
                                <div className="mt-3 grid grid-cols-4 gap-1.5">
                                    {days.map((day) => {
                                        const on = occupants(day.iso);
                                        const isSelected = cookDate === day.iso;
                                        return (
                                            <button
                                                key={day.iso}
                                                type="button"
                                                onClick={() => setCookDate(day.iso)}
                                                className={`cursor-pointer rounded-lg px-1 py-2 text-center text-xs font-semibold transition-colors ${
                                                    isSelected
                                                        ? 'bg-indigo-600/30 text-white ring-1 ring-indigo-500'
                                                        : 'bg-white/5 text-gray-300 ring-1 ring-white/5 hover:bg-white/10'
                                                }`}
                                            >
                                                {day.name} {day.number}
                                                <span className={`mt-0.5 block truncate text-[10px] font-medium ${
                                                    on.length === 0 ? 'text-emerald-300/80' : 'text-gray-500'
                                                }`}>
                                                    {on.length === 0
                                                        ? 'free'
                                                        : on.map(({batch}) => batch.recipeName).join(', ')}
                                                </span>
                                            </button>
                                        );
                                    })}
                                    <button
                                        type="button"
                                        onClick={() => setCookDate(QUEUE)}
                                        className={`col-span-4 cursor-pointer rounded-lg px-2 py-2 text-center text-xs font-semibold transition-colors ${
                                            cookDate === QUEUE
                                                ? 'bg-indigo-600/30 text-white ring-1 ring-indigo-500'
                                                : 'bg-white/5 text-gray-300 ring-1 ring-white/5 hover:bg-white/10'
                                        }`}
                                    >
                                        No day yet
                                        <span className="mt-0.5 block text-[10px] font-medium text-gray-500">
                                            park it in the To Cook queue
                                        </span>
                                    </button>
                                </div>

                                {/* Navigating away leaves the chosen night selected but off screen, so
                                    say where it went rather than showing seven unselected days. */}
                                {cookDate !== QUEUE && cookWeek !== viewWeek && (
                                    <button
                                        type="button"
                                        onClick={() => setViewWeek(cookWeek)}
                                        className="mt-2 w-full cursor-pointer rounded-lg bg-white/[0.03] px-2 py-1.5 text-left text-[11px] text-gray-400 ring-1 ring-white/5 transition-colors hover:bg-white/10 hover:text-white"
                                    >
                                        Cooking <strong className="font-semibold text-gray-200">{formatDayChip(cookDate)}</strong>
                                        {', '}{weekLabel(fromIsoDate(cookWeek), new Date()).toLowerCase()} — show it
                                    </button>
                                )}
                            </section>

                            {/* ------------------------------------------------------ portions */}
                            <section className="border-b border-white/5 p-4">
                                <h3 className={sectionTitle}>Portions</h3>

                                {recipe.servings === null ? (
                                    <>
                                        <p className="mt-2 text-xs leading-relaxed text-gray-500">
                                            We couldn't read a serving size for this recipe. Set
                                            <strong className="font-semibold text-gray-300"> Serves </strong>
                                            on it to plan leftovers automatically next time.
                                        </p>
                                        <Stepper
                                            label="This batch makes"
                                            suffix={manualMeals === 1 ? 'meal' : 'meals'}
                                            value={manualMeals}
                                            min={1}
                                            max={12}
                                            onChange={setManualMeals}
                                        />
                                    </>
                                ) : (
                                    <>
                                        <Stepper
                                            label="Cook enough for"
                                            suffix={serves === 1 ? 'person' : 'people'}
                                            value={serves}
                                            min={1}
                                            max={20}
                                            onChange={setServesOverride}
                                        />
                                        <Stepper
                                            label="People eating"
                                            value={people}
                                            min={1}
                                            max={8}
                                            onChange={setPeopleOverride}
                                        />
                                        <p className="mt-2 text-xs text-gray-500">
                                            {serves} ÷ {people} ={' '}
                                            <strong className="font-semibold text-gray-300">
                                                {mealsTotal} {mealsTotal === 1 ? 'meal' : 'meals'}
                                            </strong>
                                            {isScaled(scale) && (
                                                <span className="mt-1 block">
                                                    The recipe serves {recipe.servings}, so its
                                                    amounts go on the list{' '}
                                                    <strong className="font-semibold text-gray-300">
                                                        ×{Number(scale.toFixed(2))}
                                                    </strong>
                                                    . The saved recipe is left as it is.
                                                </span>
                                            )}
                                        </p>
                                    </>
                                )}

                                {cookDate !== QUEUE && leftoverCount > 0 && (
                                    <div className="mt-3">
                                        <p className="mb-1 text-xs text-gray-600">
                                            Where do the other {leftoverCount === 1 ? 'meal goes' : `${leftoverCount} meals go`}?
                                        </p>
                                        {leftovers.map((choice, index) => (
                                            <LeftoverRow
                                                key={index}
                                                index={index}
                                                choice={choice}
                                                days={cookWeekDays}
                                                cookDate={cookDate}
                                                onChange={setLeftover}
                                            />
                                        ))}
                                    </div>
                                )}
                            </section>

                            {/* --------------------------------------------------- ingredients */}
                            <section className="p-4">
                                <button
                                    type="button"
                                    onClick={() => setIngredientsOpen((open) => !open)}
                                    className="flex w-full cursor-pointer items-center justify-between gap-3 text-left"
                                >
                                    <span>
                                        <span className="block text-xs font-semibold text-gray-300">
                                            Ingredients
                                        </span>
                                        <span className="mt-0.5 block text-xs text-gray-500">
                                            {titles.length} of {recipe.ingredients.length} going on the list
                                        </span>
                                    </span>
                                    <span className="text-xs text-gray-500">
                                        {ingredientsOpen ? '▲' : '▼'}
                                    </span>
                                </button>
                                {ingredientsOpen && (
                                    <div className="-mx-4 mt-2">
                                        <IngredientChecklist
                                            ingredients={recipe.ingredients}
                                            selection={selection}
                                            className="max-h-60 overflow-y-auto"
                                        />
                                    </div>
                                )}
                            </section>
                        </div>

                        <div className="flex flex-wrap items-center gap-2.5 border-t border-white/5 p-4">
                            <span className="flex-1 text-xs leading-relaxed text-gray-500">
                                <strong className="font-semibold text-gray-200">
                                    {cookDate === QUEUE ? 'Queue' : formatDayChip(cookDate)}
                                </strong>
                                {placedLeftovers > 0 && ` · ${placedLeftovers} leftover ${placedLeftovers === 1 ? 'meal' : 'meals'}`}
                                {frozen > 0 && ` · ${frozen} frozen`}
                                {` · `}
                                <strong className="font-semibold text-gray-200">{titles.length}</strong>
                                {` ${titles.length === 1 ? 'item' : 'items'}`}
                                {planStatus === 'failed' && (
                                    <span className="mt-1 block text-red-300">
                                        That didn't save. Try again?
                                    </span>
                                )}
                            </span>
                            <button
                                type="button"
                                onClick={confirm}
                                disabled={planStatus === 'loading'}
                                className="cursor-pointer rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-gray-700 disabled:text-gray-400"
                            >
                                {planStatus === 'loading'
                                    ? 'Planning…'
                                    : cookDate === QUEUE ? 'Add to queue' : 'Plan it'}
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

function Stepper({label, suffix, value, min, max, onChange}: {
    label: string;
    suffix?: string;
    value: number;
    min: number;
    max: number;
    onChange: (value: number) => void;
}) {
    const button = 'flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg bg-white/5 text-gray-300 transition-colors hover:bg-white/15 hover:text-white disabled:cursor-default disabled:opacity-30';

    return (
        <div className="mt-3 flex items-center justify-between gap-3">
            <span className="text-sm text-gray-400">{label}</span>
            <span className="inline-flex items-center gap-2.5">
                <button type="button" onClick={() => onChange(value - 1)} disabled={value <= min}
                        aria-label={`${label}: fewer`} className={button}>
                    <Minus size={14}/>
                </button>
                <span className="min-w-4 text-center text-sm font-semibold text-white">{value}</span>
                <button type="button" onClick={() => onChange(value + 1)} disabled={value >= max}
                        aria-label={`${label}: more`} className={button}>
                    <Plus size={14}/>
                </button>
                {suffix && <span className="text-sm text-gray-400">{suffix}</span>}
            </span>
        </div>
    );
}

function LeftoverRow({index, choice, days, cookDate, onChange}: {
    index: number;
    choice: LeftoverChoice;
    days: { iso: string; name: string; number: number }[];
    cookDate: string;
    onChange: (index: number, choice: LeftoverChoice) => void;
}) {
    const summary = choice.mode === 'day'
        ? formatDayChip(choice.plannedDate)
        : choice.mode === 'freeze' ? 'In the freezer' : 'Not planned';

    const pill = 'h-7 min-w-7 cursor-pointer rounded-lg px-1.5 text-[11px] font-bold transition-colors';

    return (
        <div className="flex items-center gap-2.5 border-t border-white/5 py-2">
            <span className="w-24 shrink-0 text-xs text-gray-400">
                Leftover {index + 1}
                <span className={`mt-0.5 block text-[11px] font-semibold ${
                    choice.mode === 'day' ? 'text-amber-200'
                        : choice.mode === 'freeze' ? 'text-sky-200' : 'text-gray-500'
                }`}>
                    {summary}
                </span>
            </span>
            <span className="flex flex-1 flex-wrap gap-1">
                {days.map((day) => {
                    // Nothing can be eaten before it has been cooked.
                    const tooEarly = day.iso <= cookDate;
                    const isSelected = choice.mode === 'day' && choice.plannedDate === day.iso;
                    return (
                        <button
                            key={day.iso}
                            type="button"
                            disabled={tooEarly}
                            title={tooEarly ? 'Before you cook it' : `${day.name} ${day.number}`}
                            onClick={() => onChange(index, {mode: 'day', plannedDate: day.iso})}
                            className={`${pill} ${
                                isSelected
                                    ? 'bg-amber-500/25 text-amber-200 ring-1 ring-amber-500/45'
                                    : tooEarly
                                        ? 'cursor-default text-gray-700'
                                        : 'bg-white/5 text-gray-500 hover:bg-white/10 hover:text-white'
                            }`}
                        >
                            {day.name.charAt(0)}
                        </button>
                    );
                })}
                <button
                    type="button"
                    onClick={() => onChange(index, {mode: 'freeze'})}
                    title="Freeze it"
                    className={`${pill} ${
                        choice.mode === 'freeze'
                            ? 'bg-sky-500/25 text-sky-200 ring-1 ring-sky-500/40'
                            : 'bg-white/5 text-gray-500 hover:bg-white/10'
                    }`}
                >
                    ❄️
                </button>
                <button
                    type="button"
                    onClick={() => onChange(index, {mode: 'skip'})}
                    title="Leave it spare"
                    className={`${pill} ${
                        choice.mode === 'skip'
                            ? 'bg-white/10 text-gray-300 ring-1 ring-white/20'
                            : 'bg-white/5 text-gray-500 hover:bg-white/10'
                    }`}
                >
                    skip
                </button>
            </span>
        </div>
    );
}

function PlannedRecap({recipe, cookDate, placedLeftovers, frozen, itemsAdded, onClose, onOpenPlanner}: {
    recipe: ExtractedRecipeDto;
    cookDate: string | null;
    placedLeftovers: number;
    frozen: number;
    itemsAdded: number;
    onClose: () => void;
    onOpenPlanner?: () => void;
}) {
    const line = 'flex items-start gap-3 rounded-xl bg-white/[0.03] px-3 py-2.5 text-sm text-gray-300 ring-1 ring-white/5';

    return (
        <>
            <div className="px-6 pb-5 pt-7 text-center">
                <span className="mx-auto mb-4 flex h-13 w-13 items-center justify-center rounded-full bg-emerald-500/15 p-3 text-emerald-300">
                    <Check size={24}/>
                </span>
                <h2 className="text-base font-semibold text-white">{recipe.name} is planned</h2>
                <p className="mt-1 text-xs text-gray-500">
                    {cookDate ? `Cooking ${formatDayChip(cookDate)}` : 'Waiting in your To Cook queue'}
                </p>

                <div className="mx-auto mt-5 flex max-w-sm flex-col gap-2 text-left">
                    <div className={line}>
                        <span className="w-5 shrink-0 text-center">{cookDate ? '🍳' : '📋'}</span>
                        <span>
                            {cookDate ? `Cook on ${formatDayChip(cookDate)}` : 'Added to the queue'}
                            <span className="mt-0.5 block text-xs text-gray-500">
                                {cookDate ? 'Change it any time on the To Cook page' : 'Give it a day whenever you like'}
                            </span>
                        </span>
                    </div>
                    {placedLeftovers > 0 && (
                        <div className={line}>
                            <span className="w-5 shrink-0 text-center">↩</span>
                            <span>
                                {placedLeftovers} leftover {placedLeftovers === 1 ? 'meal' : 'meals'} placed
                                <span className="mt-0.5 block text-xs text-gray-500">
                                    No extra shopping for those nights
                                </span>
                            </span>
                        </div>
                    )}
                    {frozen > 0 && (
                        <div className={line}>
                            <span className="w-5 shrink-0 text-center">❄️</span>
                            <span>
                                {frozen} {frozen === 1 ? 'meal' : 'meals'} to the freezer
                                <span className="mt-0.5 block text-xs text-gray-500">
                                    Pull into any future week
                                </span>
                            </span>
                        </div>
                    )}
                    <div className={line}>
                        <span className="w-5 shrink-0 text-center">🛒</span>
                        <span>
                            {itemsAdded} {itemsAdded === 1 ? 'item' : 'items'} added to your shopping list
                        </span>
                    </div>
                </div>
            </div>

            <div className="flex flex-wrap justify-end gap-2.5 border-t border-white/5 p-4">
                <button
                    type="button"
                    onClick={onClose}
                    className="cursor-pointer rounded-lg bg-white/5 px-4 py-2.5 text-sm font-semibold text-gray-300 ring-1 ring-white/5 transition-colors hover:bg-white/10 hover:text-white"
                >
                    Keep browsing
                </button>
                {onOpenPlanner && (
                    <button
                        type="button"
                        onClick={onOpenPlanner}
                        className="cursor-pointer rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-500"
                    >
                        Open To Cook
                    </button>
                )}
            </div>
        </>
    );
}
