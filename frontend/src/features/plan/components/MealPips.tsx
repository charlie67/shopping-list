import type {PlanBatchDto} from '@/common/types/plan';
import {isQueued, type PipKind, pipsFor} from '../portions';

// Straight from the mockup: the cook night, a leftover with a home, a portion in the freezer, and
// a meal the batch makes that has nowhere to go yet.
const PIP_CLASS: Record<PipKind, string> = {
    cook: 'bg-indigo-600',
    leftover: 'bg-amber-500/75',
    frozen: 'bg-sky-500/60',
    spare: 'ring-1 ring-inset ring-white/20',
};

const PIP_TITLE: Record<PipKind, string> = {
    cook: 'Cooked fresh',
    leftover: 'Leftovers',
    frozen: 'In the freezer',
    spare: 'Not placed yet',
};

export function MealPips({batch}: { batch: PlanBatchDto }) {
    const pips = pipsFor(batch);
    const queued = isQueued(batch);
    const unplaced = queued ? 0 : batch.spareMeals;

    return (
        <span className="inline-flex items-center gap-1.5">
            <span className="inline-flex items-center gap-1">
                {pips.map((kind, index) => (
                    <span
                        key={index}
                        title={PIP_TITLE[kind]}
                        className={`h-3 w-3 shrink-0 rounded ${PIP_CLASS[kind]}`}
                    />
                ))}
            </span>
            <span className="text-xs text-gray-500">
                makes {batch.mealsTotal} {batch.mealsTotal === 1 ? 'meal' : 'meals'}
                {unplaced > 0 && ` · ${unplaced} unplaced`}
            </span>
        </span>
    );
}
