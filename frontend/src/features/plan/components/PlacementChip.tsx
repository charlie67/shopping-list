import {X} from 'lucide-react';
import type {MealPlacement} from '@/common/types/plan';
import {formatDayChip} from '@/common/date/week';

interface Props {
    placement: MealPlacement;
    plannedDate: string | null;
    onClick?: () => void;
    onRemove?: () => void;
}

const CHIP_CLASS: Record<MealPlacement, string> = {
    COOK: 'bg-indigo-600/20 text-indigo-200 hover:bg-indigo-600/30',
    LEFTOVER: 'bg-amber-500/15 text-amber-200 ring-1 ring-inset ring-amber-500/30 hover:bg-amber-500/25',
    FROZEN: 'bg-sky-500/15 text-sky-200 ring-1 ring-inset ring-sky-500/30 hover:bg-sky-500/25',
};

/** One meal's home: the night it is cooked, the night it is eaten, or the freezer. */
export function PlacementChip({placement, plannedDate, onClick, onRemove}: Props) {
    const label = placement === 'FROZEN'
        ? '❄️ Frozen'
        : `${placement === 'LEFTOVER' ? '↩ ' : ''}${plannedDate ? formatDayChip(plannedDate) : ''}`;

    return (
        <span
            className={`inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1 text-xs font-semibold transition-colors ${CHIP_CLASS[placement]} ${onClick ? 'cursor-pointer' : ''}`}
        >
            <button
                type="button"
                onClick={onClick}
                disabled={!onClick}
                className="cursor-pointer disabled:cursor-default"
            >
                {label}
            </button>
            {onRemove && (
                <button
                    type="button"
                    onClick={onRemove}
                    aria-label="Take this meal off"
                    title="Back to the pool"
                    className="cursor-pointer opacity-60 transition-opacity hover:opacity-100"
                >
                    <X size={12}/>
                </button>
            )}
        </span>
    );
}
