import type {PlanBatchDto, PlannedMealDto} from '@/common/types/plan';
import {dayName, fromIsoDate, isSameDay} from '@/common/date/week';
import {mealsForDay} from '../portions';
import {useMealDrag, useMealDrop} from '../dnd/useMealDrag';

interface Props {
    isoDate: string;
    batches: PlanBatchDto[];
    /** Tapping a chip decides where the meal goes; the recipe is reachable from that sheet. */
    onSelectMeal: (batch: PlanBatchDto, mealId: string) => void;
}

/**
 * One night of the week, and a place to drop a meal on. Seven of these sit in a grid from `sm` up
 * and stack into a list below it — seven columns with readable chips do not fit a phone, and a week
 * you are meant to read at a glance should not scroll sideways.
 */
export function DayCell({isoDate, batches, onSelectMeal}: Props) {
    const date = fromIsoDate(isoDate);
    const meals = mealsForDay(batches, isoDate);
    const isToday = isSameDay(date, new Date());
    const {dropProps, isOver} = useMealDrop({kind: 'day', isoDate});

    return (
        <div
            {...dropProps}
            className={`rounded-xl p-2.5 ring-1 ring-inset transition-colors sm:min-h-[6rem] ${
                isOver
                    ? 'bg-indigo-600/20 ring-2 ring-indigo-400'
                    : isToday
                        ? 'bg-indigo-600/10 ring-indigo-500/40'
                        : 'bg-white/[0.03] ring-white/5'
            }`}
        >
            <div className="flex items-baseline gap-2 sm:block">
                <span className={`text-[10px] font-semibold uppercase tracking-wider ${
                    isToday ? 'text-indigo-300' : 'text-gray-500'
                }`}>
                    {dayName(date)}
                </span>
                <span className={`text-sm font-semibold sm:mt-0.5 sm:block ${
                    isToday ? 'text-white' : 'text-gray-300'
                }`}>
                    {date.getDate()}
                </span>
            </div>

            <div className="mt-1.5 flex flex-wrap gap-1 sm:flex-col">
                {meals.length === 0 ? (
                    <span className="rounded-md border border-dashed border-white/10 px-1.5 py-1 text-center text-[10px] text-gray-600 sm:block">
                        empty
                    </span>
                ) : (
                    meals.map(({batch, meal}) => (
                        <DayChip
                            key={meal.id}
                            batch={batch}
                            meal={meal}
                            onSelect={() => onSelectMeal(batch, meal.id)}
                        />
                    ))
                )}
            </div>
        </div>
    );
}

function DayChip({batch, meal, onSelect}: {
    batch: PlanBatchDto;
    meal: PlannedMealDto;
    onSelect: () => void;
}) {
    const {dragProps, isDragging} = useMealDrag(
        {kind: 'meal', mealId: meal.id, placement: meal.placement, batch},
        batch.recipeName,
    );

    return (
        <button
            {...dragProps}
            type="button"
            onClick={onSelect}
            title={`${batch.recipeName} — drag it, or tap to move it`}
            className={`block max-w-full cursor-grab select-none truncate rounded-md px-1.5 py-1 text-left text-[10px] font-semibold transition-colors active:cursor-grabbing ${
                isDragging ? 'opacity-40' : ''
            } ${
                meal.placement === 'COOK'
                    ? 'bg-indigo-600/20 text-indigo-200 hover:bg-indigo-600/35'
                    : 'bg-amber-500/15 text-amber-200 ring-1 ring-inset ring-amber-500/30 hover:bg-amber-500/25'
            }`}
        >
            {meal.placement === 'COOK' ? '' : '↩ '}{batch.recipeName}
        </button>
    );
}
