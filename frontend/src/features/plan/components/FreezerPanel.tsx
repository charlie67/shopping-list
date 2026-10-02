import {X} from 'lucide-react';
import type {FrozenMealDto} from '@/common/types/plan';
import {formatDayChip} from '@/common/date/week';
import {useMealDrag, useMealDrop} from '../dnd/useMealDrag';

interface Props {
    freezer: FrozenMealDto[];
    onOpenRecipe: (recipeId: string) => void;
    onUseMeal?: (meal: FrozenMealDto) => void;
    onUnfreeze?: (mealId: string) => void;
}

/**
 * Portions put by. Global rather than week-scoped, which is the point of it: a batch cooked this
 * Sunday can feed a night three weeks out.
 */
export function FreezerPanel({freezer, onOpenRecipe, onUseMeal, onUnfreeze}: Props) {
    const {dropProps, isOver} = useMealDrop({kind: 'freezer'});

    return (
        <div
            {...dropProps}
            className={`rounded-2xl bg-gray-900 ring-1 transition-colors ${
                isOver ? 'ring-2 ring-sky-400' : 'ring-white/5'
            }`}
        >
            <div className="flex items-center justify-between px-4 pb-3 pt-4">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                    ❄️ Freezer
                </h3>
                <span className="text-xs text-gray-500">
                    {freezer.length > 0
                        ? `${freezer.length} ${freezer.length === 1 ? 'meal' : 'meals'}`
                        : 'empty'}
                </span>
            </div>

            <div className="flex flex-col gap-1.5 px-3 pb-3.5">
                {freezer.length === 0 ? (
                    <p className="rounded-xl border border-dashed border-white/10 px-3 py-5 text-center text-xs text-gray-600">
                        Nothing frozen
                    </p>
                ) : (
                    freezer.map((meal) => (
                        <FrozenRow
                            key={meal.id}
                            meal={meal}
                            onOpenRecipe={onOpenRecipe}
                            onUseMeal={onUseMeal}
                            onUnfreeze={onUnfreeze}
                        />
                    ))
                )}
            </div>
        </div>
    );
}

function FrozenRow({meal, onOpenRecipe, onUseMeal, onUnfreeze}: {
    meal: FrozenMealDto;
    onOpenRecipe: (recipeId: string) => void;
    onUseMeal?: (meal: FrozenMealDto) => void;
    onUnfreeze?: (mealId: string) => void;
}) {
    const {dragProps, isDragging} = useMealDrag({
        kind: 'frozen',
        mealId: meal.id,
        recipeId: meal.recipeId,
        recipeName: meal.recipeName,
    }, meal.recipeName);

    return (
                        <div
                            {...dragProps}
                            title="Drag onto a day to eat it"
                            className={`flex cursor-grab select-none items-center gap-2.5 rounded-xl bg-sky-500/10 p-2 ring-1 ring-inset ring-sky-500/25 transition-colors hover:bg-sky-500/15 active:cursor-grabbing ${
                                isDragging ? 'opacity-40' : ''
                            }`}
                        >
                            <span className="h-8 w-8 shrink-0 overflow-hidden rounded-lg bg-gray-800">
                                {meal.recipeImageUrl && (
                                    <img src={meal.recipeImageUrl} alt="" className="h-full w-full object-cover"/>
                                )}
                            </span>
                            <span className="min-w-0 flex-1">
                                <button
                                    type="button"
                                    onClick={() => onOpenRecipe(meal.recipeId)}
                                    className="block cursor-pointer truncate text-left text-xs text-gray-200 hover:underline hover:underline-offset-2"
                                >
                                    {meal.recipeName}
                                </button>
                                {meal.cookedOn && (
                                    <span className="mt-0.5 block text-[10px] text-sky-300">
                                        cooked {formatDayChip(meal.cookedOn)}
                                    </span>
                                )}
                            </span>
                            {onUseMeal && (
                                <button
                                    type="button"
                                    onClick={() => onUseMeal(meal)}
                                    className="shrink-0 cursor-pointer rounded-lg bg-white/5 px-2 py-1 text-[11px] font-semibold text-sky-200 transition-colors hover:bg-white/15"
                                >
                                    Use it
                                </button>
                            )}
                            {onUnfreeze && (
                                <button
                                    type="button"
                                    onClick={() => onUnfreeze(meal.id)}
                                    aria-label="Take out of the freezer"
                                    title="Back to the pool"
                                    className="shrink-0 cursor-pointer rounded-lg p-1 text-gray-500 transition-colors hover:bg-white/10 hover:text-white"
                                >
                                    <X size={12}/>
                                </button>
                            )}
                        </div>
    );
}
