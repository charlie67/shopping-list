import type {PlanBatchDto} from '@/common/types/plan';
import {coverage, spareTotal} from '../portions';
import {useMealDrop} from '../dnd/useMealDrag';

interface Props {
    batches: PlanBatchDto[];
    isoDates: string[];
    frozenCount: number;
}

/** How much of the week is actually fed, and what is left over to spend. */
export function CoverageBar({batches, isoDates, frozenCount}: Props) {
    const {cooked, leftovers, empty} = coverage(batches, isoDates);
    const spare = spareTotal(batches);
    // Dropping a meal here takes it off its day without throwing it away.
    const {dropProps, isOver, isDragActive} = useMealDrop({kind: 'pool'});
    const percent = (nights: number) => `${(nights / isoDates.length) * 100}%`;

    return (
        <div className="px-4 pb-4">
            <div className="flex h-2 overflow-hidden rounded-full bg-white/5">
                <span className="block bg-indigo-600 transition-[width]" style={{width: percent(cooked)}}/>
                <span className="block bg-amber-500 transition-[width]" style={{width: percent(leftovers)}}/>
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-gray-400">
                <span className="inline-flex items-center gap-1.5">
                    <i className="h-2 w-2 rounded-sm bg-indigo-600"/>
                    {cooked} cooked fresh
                </span>
                <span className="inline-flex items-center gap-1.5">
                    <i className="h-2 w-2 rounded-sm bg-amber-500"/>
                    {leftovers} from leftovers
                </span>
                <span className="inline-flex items-center gap-1.5">
                    <i className="h-2 w-2 rounded-sm bg-white/15"/>
                    {empty} {empty === 1 ? 'night' : 'nights'} unplanned
                </span>
                <span
                    {...dropProps}
                    className={`-mx-2 rounded-lg px-2 py-0.5 transition-colors sm:ml-auto ${
                        isOver
                            ? 'bg-amber-500/20 text-amber-200 ring-1 ring-amber-500/50'
                            : isDragActive
                                ? 'text-gray-400 ring-1 ring-dashed ring-white/15'
                                : 'text-gray-500'
                    }`}
                >
                    <span className="font-semibold text-amber-200">{spare}</span>
                    {' '}spare {spare === 1 ? 'meal' : 'meals'} in the pool
                    {frozenCount > 0 && ` · ${frozenCount} frozen`}
                </span>
            </div>
        </div>
    );
}
