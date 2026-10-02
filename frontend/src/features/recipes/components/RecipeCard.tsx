import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {CookingPot, ExternalLink, Plus} from 'lucide-react';
import {ExtractedRecipeDto} from '@/common/types/recipe';
import {IngredientPicker} from './IngredientPicker';
import {PlanRecipeSheet} from './PlanRecipeSheet';

interface Props {
    recipe: ExtractedRecipeDto;
    onOpen: () => void;
}

export function RecipeCard({recipe, onOpen}: Props) {
    const navigate = useNavigate();
    const [showPicker, setShowPicker] = useState(false);
    const [showPlanner, setShowPlanner] = useState(false);

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
                {/* Wraps because the card is a quarter of the grid at xl and the three labels do not
                    fit on one line; the card clips its overflow, so without this the last button
                    disappears rather than moving down. */}
                <div className="mt-auto flex flex-wrap items-center gap-2 pt-4">
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
                            setShowPicker(true);
                        }}
                        className="cursor-pointer flex items-center gap-1.5 rounded-lg bg-indigo-600/20 px-3 py-2 text-xs font-medium text-indigo-300 hover:bg-indigo-600/30 hover:text-indigo-200 transition-colors"
                    >
                        <Plus size={14}/>
                        Ingredients
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            setShowPlanner(true);
                        }}
                        className="cursor-pointer flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-2 text-xs font-medium text-white hover:bg-indigo-500 transition-colors"
                    >
                        <CookingPot size={14}/>
                        Plan to cook
                    </button>
                </div>
            </div>
            {showPicker && (
                <div onClick={(e) => e.stopPropagation()}>
                    <IngredientPicker
                        recipeName={recipe.name}
                        ingredients={recipe.ingredients}
                        onClose={() => setShowPicker(false)}
                    />
                </div>
            )}
            {showPlanner && (
                <div onClick={(e) => e.stopPropagation()}>
                    <PlanRecipeSheet
                        recipe={recipe}
                        onClose={() => setShowPlanner(false)}
                        onOpenPlanner={() => navigate('/to-cook')}
                    />
                </div>
            )}
        </div>
    );
}
