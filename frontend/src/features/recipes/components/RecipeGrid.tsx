import {useCallback, useState} from 'react';
import {Loader2} from 'lucide-react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useInfiniteScroll} from '@/common/hooks/useInfiniteScroll';
import {
    fetchRecipesPage,
    selectRecipeItems,
    selectRecipesHasMore,
    selectRecipesPage,
    selectRecipesStatus,
} from '../recipesSlice';
import {RecipeCard} from './RecipeCard';
import {RecipeDetailModal} from './RecipeDetailModal';

export function RecipeGrid() {
    const dispatch = useAppDispatch();
    const items = useAppSelector(selectRecipeItems);
    const hasMore = useAppSelector(selectRecipesHasMore);
    const status = useAppSelector(selectRecipesStatus);
    const currentPage = useAppSelector(selectRecipesPage);
    const [selectedRecipeId, setSelectedRecipeId] = useState<string | null>(null);

    const loadMore = useCallback(() => {
        dispatch(fetchRecipesPage(currentPage + 1));
    }, [dispatch, currentPage]);

    const {sentinelRef} = useInfiniteScroll({
        hasMore,
        isLoading: status === 'loading',
        onLoadMore: loadMore,
    });

    if (Object.keys(items).length === 0 && status !== 'loading') {
        return (
            <div className="py-16 text-center text-gray-500">
                <p className="text-lg">No recipes yet</p>
                <p className="mt-1 text-sm">Paste a recipe URL above to get started</p>
            </div>
        );
    }

    const selectedRecipe = selectedRecipeId ? items[selectedRecipeId] : null;

    return (
        <>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {Object.values(items).map((recipe) => (
                    <RecipeCard
                        key={recipe.id}
                        recipe={recipe}
                        onOpen={() => setSelectedRecipeId(recipe.id)}
                    />
                ))}
            </div>
            <div ref={sentinelRef}/>
            {status === 'loading' && (
                <div className="flex justify-center py-4">
                    <Loader2 size={24} className="animate-spin text-gray-500"/>
                </div>
            )}
            {selectedRecipe && (
                <RecipeDetailModal
                    recipe={selectedRecipe}
                    onClose={() => setSelectedRecipeId(null)}
                />
            )}
        </>
    );
}
