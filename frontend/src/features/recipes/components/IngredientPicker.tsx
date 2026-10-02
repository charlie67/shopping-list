import {useEffect} from 'react';
import {Plus, X} from 'lucide-react';
import {RecipeIngredient} from '@/common/types/recipe';
import {useAppDispatch} from '@/common/hooks/redux';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {addShoppingListItems} from '@/features/shopping-list/shoppingListSlice';
import {useIngredientSelection} from '../useIngredientSelection';
import {IngredientChecklist} from './IngredientChecklist';

interface Props {
    recipeName: string;
    ingredients: RecipeIngredient[];
    /** Scales the amounts when opened from a batch cooked for more than the recipe serves. */
    scale?: number;
    onClose: () => void;
}

/** Ingredients onto the shopping list, without planning anything. */
export function IngredientPicker({recipeName, ingredients, scale = 1, onClose}: Props) {
    const dispatch = useAppDispatch();
    const selection = useIngredientSelection(ingredients, scale);

    useEscapeKey(onClose);

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    const addSelected = () => {
        // One request for the lot rather than one per ingredient, so the list broadcasts once.
        const titles = selection.titles();
        if (titles.length > 0) {
            dispatch(addShoppingListItems(titles));
        }
        onClose();
    };

    return (
        <div
            // cursor-default because `cursor` is inherited: this sheet is a DOM child of whatever
            // opened it, and a RecipeCard is itself one big cursor-pointer button, so without this
            // every inch of the sheet claims to be clickable.
            className="fixed inset-0 z-50 flex h-[100dvh] cursor-default items-stretch justify-center bg-black/70 backdrop-blur-sm sm:items-center sm:p-4"
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

                <div className="flex flex-1 flex-col overflow-y-auto">
                    <IngredientChecklist ingredients={ingredients} selection={selection}/>
                </div>

                <div className="border-t border-white/5 p-4">
                    <button
                        onClick={addSelected}
                        disabled={selection.selected.size === 0}
                        className="flex w-full cursor-pointer items-center justify-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-gray-700 disabled:text-gray-400"
                    >
                        <Plus size={16}/>
                        Add {selection.selected.size > 0 ? selection.selected.size : ''}{' '}
                        {selection.selected.size === 1 ? 'ingredient' : 'ingredients'}
                    </button>
                </div>
            </div>
        </div>
    );
}
