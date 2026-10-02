import {Check} from 'lucide-react';
import type {RecipeIngredient} from '@/common/types/recipe';
import type {IngredientSelection} from '../useIngredientSelection';

interface Props {
    ingredients: RecipeIngredient[];
    selection: IngredientSelection;
    className?: string;
}

const chipClass = (active: boolean) =>
    `cursor-pointer rounded-full px-2 py-1 text-xs transition-colors ${
        active
            ? 'bg-indigo-500/25 text-indigo-200 ring-1 ring-indigo-500/40 hover:bg-indigo-500/40 hover:text-white'
            : 'bg-white/5 text-gray-400 ring-1 ring-white/5 hover:bg-white/10 hover:text-gray-200'
    }`;

/** The tickable ingredient list, with each row's shortened or original wording. */
export function IngredientChecklist({ingredients, selection, className = ''}: Props) {
    const {selected, useOriginal, shortTexts, allSelected, textFor, toggle, toggleAll, setOriginal} = selection;

    return (
        <>
            <div className="flex items-center justify-between px-4 py-2">
                <span className="text-xs text-gray-500">
                    {selected.size} of {ingredients.length} selected
                </span>
                <button
                    onClick={toggleAll}
                    className="cursor-pointer text-xs font-medium text-indigo-400 transition-colors hover:text-indigo-300"
                >
                    {allSelected ? 'Deselect all' : 'Select all'}
                </button>
            </div>

            <ul className={`flex flex-col gap-1.5 px-4 pb-2 pt-1 ${className}`}>
                {ingredients.map((ing, i) => {
                    const isSelected = selected.has(i);
                    const shortText = shortTexts[i];
                    const isOriginal = useOriginal.has(i);
                    return (
                        <li
                            key={`${ing.fullText}-${i}`}
                            className={`rounded-lg ring-1 transition-colors ${
                                isSelected
                                    ? 'bg-indigo-600/15 ring-indigo-500/40'
                                    : 'bg-white/5 ring-white/5'
                            }`}
                        >
                            <button
                                onClick={() => toggle(i)}
                                className={`flex w-full cursor-pointer items-center gap-3 px-3 py-2 text-left text-sm ${
                                    isSelected ? 'text-white' : 'text-gray-300'
                                }`}
                            >
                                <span
                                    className={`flex h-5 w-5 shrink-0 items-center justify-center rounded border transition-colors ${
                                        isSelected
                                            ? 'border-indigo-500 bg-indigo-500 text-white'
                                            : 'border-white/20 bg-transparent'
                                    }`}
                                >
                                    {isSelected && <Check size={14}/>}
                                </span>
                                <span className="min-w-0 flex-1">{textFor(i)}</span>
                            </button>
                            {shortText && (
                                <div className="flex flex-wrap gap-1.5 px-3 pb-2">
                                    <button
                                        onClick={() => setOriginal(i, false)}
                                        aria-pressed={!isOriginal}
                                        className={chipClass(!isOriginal)}
                                    >
                                        Shortened
                                    </button>
                                    <button
                                        onClick={() => setOriginal(i, true)}
                                        aria-pressed={isOriginal}
                                        className={chipClass(isOriginal)}
                                    >
                                        Original
                                    </button>
                                </div>
                            )}
                        </li>
                    );
                })}
            </ul>
        </>
    );
}
