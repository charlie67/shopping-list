import {useEffect, useState} from 'react';
import {Check, Plus, X} from 'lucide-react';
import {RecipeIngredient} from '@/common/types/recipe';
import {useAppDispatch} from '@/common/hooks/redux';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {addShoppingListItem} from '@/features/shopping-list/shoppingListSlice';
import {shortenIngredient} from '../ingredientParts';

interface Props {
    recipeName: string;
    ingredients: RecipeIngredient[];
    onClose: () => void;
}

const chipClass = (active: boolean) =>
    `cursor-pointer rounded-full px-2 py-1 text-xs transition-colors ${
        active
            ? 'bg-indigo-500/25 text-indigo-200 ring-1 ring-indigo-500/40 hover:bg-indigo-500/40 hover:text-white'
            : 'bg-white/5 text-gray-400 ring-1 ring-white/5 hover:bg-white/10 hover:text-gray-200'
    }`;

export function IngredientPicker({recipeName, ingredients, onClose}: Props) {
    const dispatch = useAppDispatch();
    const [selected, setSelected] = useState<Set<number>>(
        () => new Set(ingredients.map((_, i) => i)),
    );
    // The shortened "quantity + ingredient" text per ingredient, or null when there is nothing to
    // shorten. Computed once because the ingredient list never changes while the picker is open.
    const [shortTexts] = useState<(string | null)[]>(() => ingredients.map(shortenIngredient));
    // Indexes showing their original wording instead of the shortened version. Shortening is the
    // default, so this starts empty.
    const [useOriginal, setUseOriginal] = useState<Set<number>>(() => new Set());

    const textFor = (index: number) =>
        (!useOriginal.has(index) && shortTexts[index]) || ingredients[index].fullText;

    useEscapeKey(onClose);

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    const toggle = (index: number) => {
        setSelected((prev) => {
            const next = new Set(prev);
            if (next.has(index)) {
                next.delete(index);
            } else {
                next.add(index);
            }
            return next;
        });
    };

    const setOriginal = (index: number, original: boolean) => {
        setUseOriginal((prev) => {
            const next = new Set(prev);
            if (original) {
                next.add(index);
            } else {
                next.delete(index);
            }
            return next;
        });
    };

    const allSelected = selected.size === ingredients.length;

    const toggleAll = () => {
        setSelected(allSelected ? new Set() : new Set(ingredients.map((_, i) => i)));
    };

    const addSelected = () => {
        ingredients.forEach((_, i) => {
            if (selected.has(i)) {
                dispatch(addShoppingListItem(textFor(i)));
            }
        });
        onClose();
    };

    return (
        <div
            className="fixed inset-0 z-50 flex h-[100dvh] items-stretch justify-center bg-black/70 backdrop-blur-sm sm:items-center sm:p-4"
            onClick={onClose}
            role="dialog"
            aria-modal="true"
            aria-label={`Add ingredients from ${recipeName}`}
        >
            <div
                /* Full screen on mobile, a centred card from sm up. */
                className="relative flex h-full w-full flex-col overflow-hidden bg-gray-900 ring-1 ring-white/10 sm:h-auto sm:max-h-[85vh] sm:max-w-md sm:rounded-2xl"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-center justify-between gap-3 border-b border-white/5 p-4">
                    <div className="min-w-0">
                        <h2 className="truncate text-sm font-semibold text-white">Add ingredients</h2>
                        <p className="truncate text-xs text-gray-400">{recipeName}</p>
                    </div>
                    <button
                        onClick={onClose}
                        aria-label="Close"
                        className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full bg-white/5 text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                    >
                        <X size={16}/>
                    </button>
                </div>

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

                <ul className="flex flex-1 flex-col gap-1.5 overflow-y-auto px-4 pt-1 pb-2">
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

                <div className="border-t border-white/5 p-4">
                    <button
                        onClick={addSelected}
                        disabled={selected.size === 0}
                        className="flex w-full cursor-pointer items-center justify-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-gray-700 disabled:text-gray-400"
                    >
                        <Plus size={16}/>
                        Add {selected.size > 0 ? selected.size : ''} {selected.size === 1 ? 'ingredient' : 'ingredients'}
                    </button>
                </div>
            </div>
        </div>
    );
}
