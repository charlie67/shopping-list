import {useEffect} from 'react';
import {X} from 'lucide-react';
import type {MealPlacement, PlanBatchDto} from '@/common/types/plan';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {dayName, fromIsoDate, toIsoDate, weekDays} from '@/common/date/week';
import {cookDateOf, mealsForDay} from '../portions';

/**
 * What is being placed. A meal already in a batch, one of the batch's spares, or a portion pulled
 * out of the freezer — whose batch was cooked in some earlier week, so it is not restricted to the
 * days after a cook night.
 */
export type PlacementTarget =
    | { kind: 'meal'; mealId: string; placement: MealPlacement; batch: PlanBatchDto }
    | { kind: 'spare'; batch: PlanBatchDto }
    | { kind: 'frozen'; mealId: string; recipeId: string; recipeName: string };

interface Props {
    target: PlacementTarget;
    weekStart: string;
    batches: PlanBatchDto[];
    onPlace: (placement: MealPlacement, plannedDate: string | null) => void;
    onUnplace?: () => void;
    onOpenRecipe: (recipeId: string) => void;
    onClose: () => void;
}

export function PlacementSheet({
    target, weekStart, batches, onPlace, onUnplace, onOpenRecipe, onClose,
}: Props) {
    useEscapeKey(onClose);

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    const recipeName = target.kind === 'frozen' ? target.recipeName : target.batch.recipeName;
    const recipeId = target.kind === 'frozen' ? target.recipeId : target.batch.recipeId;

    // A batch's own cook night can move anywhere; anything eaten from it has to come afterwards.
    // A meal out of the freezer was cooked weeks ago, so no day is out of bounds for it.
    const movingTheCook = target.kind === 'meal' && target.placement === 'COOK';
    const queued = target.kind !== 'frozen' && cookDateOf(target.batch) === null;
    const earliest = target.kind === 'frozen' || movingTheCook || queued
        ? null
        : cookDateOf(target.batch);

    // Placing the first meal of a queued batch is choosing its cook night, not a leftover.
    const placementForDay: MealPlacement = queued || movingTheCook ? 'COOK' : 'LEFTOVER';

    const days = weekDays(fromIsoDate(weekStart)).map((date) => ({
        iso: toIsoDate(date),
        name: dayName(date),
        number: date.getDate(),
    }));

    const title = target.kind === 'frozen'
        ? 'Use a frozen meal'
        : target.kind === 'spare'
            ? (queued ? 'Give it a day' : 'Place a spare meal')
            : movingTheCook ? 'Move the cook night' : 'Move this meal';

    return (
        <div
            className="fixed inset-0 z-50 flex h-[100dvh] items-end justify-center bg-black/70 backdrop-blur-sm sm:items-center sm:p-4"
            onClick={onClose}
            role="dialog"
            aria-modal="true"
            aria-label={title}
        >
            <div
                className="flex w-full flex-col overflow-hidden rounded-t-2xl bg-gray-900 ring-1 ring-white/10 sm:max-w-md sm:rounded-2xl"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start justify-between gap-3 border-b border-white/5 p-4">
                    <div className="min-w-0">
                        <h2 className="truncate text-sm font-semibold text-white">{title}</h2>
                        {/* The name is the way back to the recipe, since tapping a chip moves it. */}
                        <button
                            type="button"
                            onClick={() => onOpenRecipe(recipeId)}
                            className="mt-0.5 block max-w-full cursor-pointer truncate text-left text-xs text-indigo-300 hover:underline hover:underline-offset-2"
                        >
                            {recipeName} — open the recipe
                        </button>
                    </div>
                    <button
                        onClick={onClose}
                        aria-label="Close"
                        className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full bg-white/5 text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                    >
                        <X size={16}/>
                    </button>
                </div>

                <div className="p-4">
                    {target.kind === 'frozen' && (
                        <p className="mb-3 text-xs leading-relaxed text-gray-500">
                            Nothing is added to your shopping list — this meal is already paid for.
                        </p>
                    )}

                    <div className="grid grid-cols-4 gap-1.5">
                        {days.map((day) => {
                            const tooEarly = earliest !== null && day.iso <= earliest;
                            const on = mealsForDay(batches, day.iso);
                            return (
                                <button
                                    key={day.iso}
                                    type="button"
                                    disabled={tooEarly}
                                    title={tooEarly ? 'Before you cook it' : undefined}
                                    onClick={() => onPlace(placementForDay, day.iso)}
                                    className={`rounded-lg px-1 py-2 text-center text-xs font-semibold transition-colors ${
                                        tooEarly
                                            ? 'cursor-default text-gray-700'
                                            : 'cursor-pointer bg-white/5 text-gray-300 ring-1 ring-white/5 hover:bg-indigo-600/30 hover:text-white'
                                    }`}
                                >
                                    {day.name} {day.number}
                                    <span className={`mt-0.5 block truncate text-[10px] font-medium ${
                                        tooEarly ? 'text-gray-800'
                                            : on.length === 0 ? 'text-emerald-300/80' : 'text-gray-500'
                                    }`}>
                                        {on.length === 0
                                            ? 'free'
                                            : on.map(({batch}) => batch.recipeName).join(', ')}
                                    </span>
                                </button>
                            );
                        })}
                    </div>

                    <div className="mt-3 flex flex-col gap-1.5">
                        {/* A cook night is the thing that makes the meals; it cannot itself be frozen. */}
                        {!movingTheCook && !queued && target.kind !== 'frozen' && (
                            <button
                                type="button"
                                onClick={() => onPlace('FROZEN', null)}
                                className="cursor-pointer rounded-lg bg-sky-500/15 px-3 py-2.5 text-left text-xs font-semibold text-sky-200 ring-1 ring-inset ring-sky-500/25 transition-colors hover:bg-sky-500/25"
                            >
                                ❄️ Into the freezer
                                <span className="mt-0.5 block font-medium text-sky-300/70">
                                    Keep it for another week
                                </span>
                            </button>
                        )}
                        {onUnplace && (
                            <button
                                type="button"
                                onClick={onUnplace}
                                className="cursor-pointer rounded-lg bg-white/5 px-3 py-2.5 text-left text-xs font-semibold text-gray-300 transition-colors hover:bg-white/10"
                            >
                                Back to the pool
                                <span className="mt-0.5 block font-medium text-gray-500">
                                    Keeps the meal, just without a day
                                </span>
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
