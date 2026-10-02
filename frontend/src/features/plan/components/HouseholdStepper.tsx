import {Minus, Plus} from 'lucide-react';

interface Props {
    household: number;
    onChange: (people: number) => void;
    min?: number;
    max?: number;
}

/** How many people each meal has to feed, which is what decides how far a batch stretches. */
export function HouseholdStepper({household, onChange, min = 1, max = 8}: Props) {
    const button = 'flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg bg-white/5 text-gray-300 transition-colors hover:bg-white/15 hover:text-white disabled:cursor-default disabled:opacity-30';

    return (
        <div className="inline-flex items-center gap-3 rounded-xl bg-gray-900 px-3 py-2 ring-1 ring-white/10">
            <span className="text-sm text-gray-400">
                Cooking for <span className="font-semibold text-white">{household}</span>
                {household === 1 ? ' person' : ' people'}
            </span>
            <span className="inline-flex items-center gap-2">
                <button
                    type="button"
                    onClick={() => onChange(household - 1)}
                    disabled={household <= min}
                    aria-label="Fewer people"
                    className={button}
                >
                    <Minus size={14}/>
                </button>
                <button
                    type="button"
                    onClick={() => onChange(household + 1)}
                    disabled={household >= max}
                    aria-label="More people"
                    className={button}
                >
                    <Plus size={14}/>
                </button>
            </span>
        </div>
    );
}
