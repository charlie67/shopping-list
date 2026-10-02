import {ChevronLeft, ChevronRight} from 'lucide-react';
import {
    addWeeks,
    formatWeekRange,
    fromIsoDate,
    startOfWeek,
    toIsoDate,
    weekLabel,
    weeksFromCurrent,
} from '@/common/date/week';

interface Props {
    weekStart: string;
    onChange: (weekStart: string) => void;
}

export function WeekSwitcher({weekStart, onChange}: Props) {
    const start = fromIsoDate(weekStart);
    const today = new Date();
    const isCurrentWeek = weeksFromCurrent(start, today) === 0;

    const step = (weeks: number) => onChange(toIsoDate(addWeeks(start, weeks)));

    return (
        <div className="inline-flex items-center gap-1 rounded-xl bg-gray-900 p-1 ring-1 ring-white/10">
            {/* Only worth showing once you have navigated away from it. */}
            {!isCurrentWeek && (
                <button
                    type="button"
                    onClick={() => onChange(toIsoDate(startOfWeek(today)))}
                    className="mr-1 cursor-pointer rounded-lg bg-indigo-600/25 px-2.5 py-1.5 text-xs font-semibold text-indigo-200 transition-colors hover:bg-indigo-600/40"
                >
                    This week
                </button>
            )}
            <button
                type="button"
                onClick={() => step(-1)}
                aria-label="Previous week"
                className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-white/10 hover:text-white"
            >
                <ChevronLeft size={18}/>
            </button>
            <span className="min-w-[10.5rem] px-2 text-center">
                <span className="block text-sm font-semibold text-white">
                    {weekLabel(start, today)}
                </span>
                <span className="block text-xs text-gray-500">{formatWeekRange(start)}</span>
            </span>
            <button
                type="button"
                onClick={() => step(1)}
                aria-label="Next week"
                className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-white/10 hover:text-white"
            >
                <ChevronRight size={18}/>
            </button>
        </div>
    );
}
