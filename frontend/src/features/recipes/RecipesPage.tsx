import {useEffect} from 'react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {fetchRecipesPage, selectRecipeDraft, selectRecipesPage} from './recipesSlice';
import {RecipeUrlInput} from './components/RecipeUrlInput';
import {RecipeGrid} from './components/RecipeGrid';
import {RecipeEditorModal} from './components/RecipeEditorModal';

export function RecipesPage() {
    const dispatch = useAppDispatch();
    const currentPage = useAppSelector(selectRecipesPage);
    const draft = useAppSelector(selectRecipeDraft);

    useEffect(() => {
        if (currentPage === -1) {
            dispatch(fetchRecipesPage(0));
        }
    }, [dispatch, currentPage]);

    return (
        <div className="mx-auto w-full max-w-6xl px-4 py-6">
            <RecipeUrlInput/>
            <div className="mt-6">
                <RecipeGrid/>
            </div>
            {draft && <RecipeEditorModal recipe={draft}/>}
        </div>
    );
}
