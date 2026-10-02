import type {PlanBatchDto} from '@/common/types/plan';
import {DayCell} from './DayCell';

interface Props {
    isoDates: string[];
    batches: PlanBatchDto[];
    onSelectMeal: (batch: PlanBatchDto, mealId: string) => void;
}

export function WeekRibbon({isoDates, batches, onSelectMeal}: Props) {
    return (
        <div className="grid grid-cols-1 gap-1.5 px-3 pb-3 sm:grid-cols-7 sm:gap-2">
            {isoDates.map((isoDate) => (
                <DayCell
                    key={isoDate}
                    isoDate={isoDate}
                    batches={batches}
                    onSelectMeal={onSelectMeal}
                />
            ))}
        </div>
    );
}
