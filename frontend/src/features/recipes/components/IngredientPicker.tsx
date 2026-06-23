import {useEffect, useState} from 'react';
import {Check, Plus, X} from 'lucide-react';
import {RecipeIngredient} from '@/common/types/recipe';
import {useAppDispatch} from '@/common/hooks/redux';
import {addShoppingListItem} from '@/features/shopping-list/shoppingListSlice';

interface Props {
    recipeName: string;
    ingredients: RecipeIngredient[];
    onClose: () => void;
}

export function IngredientPicker({recipeName, ingredients, onClose}: Props) {
    const dispatch = useAppDispatch();
    const [selected, setSelected] = useState<Set<number>>(
        () => new Set(ingredients.map((_, i) => i)),
    );

    useEffect(() => {
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        document.addEventListener('keydown', onKey);
        return () => document.removeEventListener('keydown', onKey);
    }, [onClose]);

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

    const allSelected = selected.size === ingredients.length;

    const toggleAll = () => {
        setSelected(allSelected ? new Set() : new Set(ingredients.map((_, i) => i)));
    };

    const addSelected = () => {
        ingredients.forEach((ing, i) => {
            if (selected.has(i)) {
                dispatch(addShoppingListItem(ing.fullText));
            }
        });
        onClose();
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-end justify-center overflow-y-auto bg-black/70 p-0 backdrop-blur-sm sm:items-center sm:p-4"
            onClick={onClose}
            role="dialog"
            aria-modal="true"
            aria-label={`Add ingredients from ${recipeName}`}
        >
            <div
                className="relative flex max-h-[85vh] w-full flex-col overflow-hidden rounded-t-2xl bg-gray-900 ring-1 ring-white/10 sm:max-w-md sm:rounded-2xl"
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
                        className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white/5 text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
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

                <ul className="flex flex-1 flex-col gap-1.5 overflow-y-auto px-4 pb-2">
                    {ingredients.map((ing, i) => {
                        const isSelected = selected.has(i);
                        return (
                            <li key={`${ing.fullText}-${i}`}>
                                <button
                                    onClick={() => toggle(i)}
                                    className={`flex w-full cursor-pointer items-center gap-3 rounded-lg px-3 py-2 text-left text-sm ring-1 transition-colors ${
                                        isSelected
                                            ? 'bg-indigo-600/15 text-white ring-indigo-500/40'
                                            : 'bg-white/5 text-gray-300 ring-white/5 hover:bg-white/10'
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
                                    <span className="min-w-0 flex-1">{ing.fullText}</span>
                                </button>
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
