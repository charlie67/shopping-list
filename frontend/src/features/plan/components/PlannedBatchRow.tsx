import {Trash2} from 'lucide-react';
import type {PlanBatchDto, PlannedMealDto} from '@/common/types/plan';
import {isQueued, orderedMeals} from '../portions';
import {useMealDrag} from '../dnd/useMealDrag';
import {MealPips} from './MealPips';
import {PlacementChip} from './PlacementChip';

interface Props {
    batch: PlanBatchDto;
    /** The household now, to spot a batch planned under a different one. */
    household: number;
    onOpenRecipe: (recipeId: string) => void;
    onRemoveBatch?: (batchId: string) => void;
    onPlaceSpare?: (batch: PlanBatchDto) => void;
    onOpenMeal?: (batch: PlanBatchDto, mealId: string) => void;
    onUnplaceMeal?: (mealId: string) => void;
}

export function PlannedBatchRow({
    batch, household, onOpenRecipe, onRemoveBatch, onPlaceSpare, onOpenMeal, onUnplaceMeal,
}: Props) {
    const queued = isQueued(batch);
    const spare = queued ? batch.mealsTotal : batch.spareMeals;
    // mealsTotal is a snapshot from when this was planned. Without saying so, a batch left over
    // from a different household reads as an arithmetic bug to whoever checks it against the yield.
    const staleHousehold = batch.cookingFor !== household;

    return (
        <div
            className={`grid grid-cols-[3rem_minmax(0,1fr)_auto] items-center gap-3 rounded-xl p-2.5 ring-1 ring-inset transition-colors ${
                queued
                    ? 'bg-transparent ring-white/5'
                    : 'bg-white/[0.02] ring-white/5 hover:bg-white/5 hover:ring-white/10'
            }`}
        >
            <button
                type="button"
                onClick={() => onOpenRecipe(batch.recipeId)}
                title="Open recipe"
                className="h-12 w-12 cursor-pointer overflow-hidden rounded-lg bg-gray-800"
            >
                {batch.recipeImageUrl && (
                    <img src={batch.recipeImageUrl} alt="" className="h-full w-full object-cover"/>
                )}
            </button>

            <div className="min-w-0">
                <button
                    type="button"
                    onClick={() => onOpenRecipe(batch.recipeId)}
                    className="cursor-pointer text-left text-sm font-semibold text-white hover:underline hover:underline-offset-2"
                >
                    {batch.recipeName}
                </button>
                <div className="mt-1.5 flex flex-wrap items-center gap-2">
                    <MealPips batch={batch}/>
                    <span className="flex flex-wrap items-center gap-1.5">
                        {orderedMeals(batch).map((meal) => (
                            <DraggableMealChip
                                key={meal.id}
                                batch={batch}
                                meal={meal}
                                onClick={onOpenMeal ? () => onOpenMeal(batch, meal.id) : undefined}
                                onRemove={onUnplaceMeal ? () => onUnplaceMeal(meal.id) : undefined}
                            />
                        ))}
                        {spare > 0 && onPlaceSpare && (
                            <DraggableSpareChip
                                batch={batch}
                                queued={queued}
                                spare={spare}
                                onClick={() => onPlaceSpare(batch)}
                            />
                        )}
                        {/* What this pot was actually planned for, which is not the recipe's own
                            yield once the batch has been scaled. Always shown when it differs from
                            the household, so reopening a batch tells you the portions you chose
                            rather than the ones the recipe was written with. */}
                        {staleHousehold && (
                            <span
                                className="text-xs text-gray-600"
                                title={`This pot was planned for ${batch.cookingFor}, not the ${household} you cook for now`}
                            >
                                serves {batch.cookingFor}
                            </span>
                        )}
                    </span>
                </div>
            </div>

            <div className="flex gap-1.5">
                {onRemoveBatch && (
                    <button
                        type="button"
                        onClick={() => onRemoveBatch(batch.id)}
                        aria-label="Remove from To Cook"
                        title="Remove from To Cook"
                        className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg bg-white/5 text-gray-400 transition-colors hover:bg-white/10 hover:text-white"
                    >
                        <Trash2 size={14}/>
                    </button>
                )}
            </div>
        </div>
    );
}

function DraggableMealChip({batch, meal, onClick, onRemove}: {
    batch: PlanBatchDto;
    meal: PlannedMealDto;
    onClick?: () => void;
    onRemove?: () => void;
}) {
    const {dragProps, isDragging} = useMealDrag(
        {kind: 'meal', mealId: meal.id, placement: meal.placement, batch},
        batch.recipeName,
    );

    return (
        <span {...dragProps}
              className={`select-none ${isDragging ? 'opacity-40' : ''}`}>
            <PlacementChip
                placement={meal.placement}
                plannedDate={meal.plannedDate}
                onClick={onClick}
                onRemove={onRemove}
            />
        </span>
    );
}

function DraggableSpareChip({batch, queued, spare, onClick}: {
    batch: PlanBatchDto;
    queued: boolean;
    spare: number;
    onClick: () => void;
}) {
    const {dragProps, isDragging} = useMealDrag(
        {kind: 'spare', batch},
        batch.recipeName,
    );

    return (
        <button
            {...dragProps}
            type="button"
            onClick={onClick}
            title="Drag onto a day, or tap to choose one"
            className={`cursor-grab select-none rounded-lg border border-dashed border-white/15 px-2.5 py-1 text-xs font-semibold text-gray-500 transition-colors hover:border-indigo-500/50 hover:bg-indigo-600/10 hover:text-indigo-300 active:cursor-grabbing ${
                isDragging ? 'opacity-40' : ''
            }`}
        >
            {queued ? '+ pick a day' : `+ place ${spare === 1 ? 'this meal' : `${spare} meals`}`}
        </button>
    );
}
