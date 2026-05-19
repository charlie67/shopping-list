import {ExternalLink, Plus} from 'lucide-react';
import {ExtractedRecipeDto, RecipeIngredient} from '@/common/types/recipe';
import {useAppDispatch} from "@/common/hooks/redux.ts";
import {addShoppingListItem} from "@/features/shopping-list/shoppingListSlice.ts";

interface Props {
    recipe: ExtractedRecipeDto;
    onOpen: () => void;
}

export function RecipeCard({recipe, onOpen}: Props) {
    const dispatch = useAppDispatch();

    const addAllIngredients = (ingredients: RecipeIngredient[]) => {
        ingredients.forEach((ing) => {
            dispatch(addShoppingListItem(ing.fullText));
        })
    }

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onOpen();
        }
    };

    return (
        <div
            onClick={onOpen}
            onKeyDown={handleKeyDown}
            role="button"
            tabIndex={0}
            className="group flex cursor-pointer flex-col overflow-hidden rounded-2xl bg-gray-900 ring-1 ring-white/5 transition-all hover:ring-white/15 hover:shadow-lg hover:shadow-indigo-500/5 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">
            {recipe.imageUrl && (
                <div className="aspect-video overflow-hidden">
                    <img
                        src={recipe.imageUrl}
                        alt={recipe.name}
                        className="h-full w-full object-cover transition-transform group-hover:scale-105"
                    />
                </div>
            )}
            <div className="flex flex-1 flex-col p-4">
                <h3 className="line-clamp-2 text-sm font-semibold text-white">{recipe.name}</h3>
                {recipe.description && (
                    <p className="mt-1 line-clamp-2 text-xs text-gray-400">{recipe.description}</p>
                )}
                <div className="mt-auto flex items-center gap-2 pt-4">
                    <a
                        href={recipe.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        onClick={(e) => e.stopPropagation()}
                        className="flex items-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 hover:bg-white/10 hover:text-white transition-colors"
                    >
                        <ExternalLink size={14}/>
                        View
                    </a>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            addAllIngredients(recipe.ingredients);
                        }}
                        className="cursor-pointer flex items-center gap-1.5 rounded-lg bg-indigo-600/20 px-3 py-2 text-xs font-medium text-indigo-300 hover:bg-indigo-600/30 hover:text-indigo-200 transition-colors"
                    >
                        <Plus size={14}/>
                        Add Ingredients
                    </button>
                </div>
            </div>
        </div>
    );
}
