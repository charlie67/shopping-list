import {useCallback} from 'react';
import {useSearchParams} from 'react-router-dom';

const SELECTED_RECIPE_PARAM = 'recipe';

/**
 * Reads and writes the recipe currently open in the detail modal, which lives in the URL so the modal
 * survives a refresh and can be linked to.
 */
export function useSelectedRecipeId(): [string | null, (id: string | null) => void] {
    const [searchParams, setSearchParams] = useSearchParams();

    const setSelectedRecipeId = useCallback((id: string | null) => {
        setSearchParams((params) => {
            const next = new URLSearchParams(params);
            if (id) {
                next.set(SELECTED_RECIPE_PARAM, id);
            } else {
                next.delete(SELECTED_RECIPE_PARAM);
            }
            return next;
        });
    }, [setSearchParams]);

    return [searchParams.get(SELECTED_RECIPE_PARAM), setSelectedRecipeId];
}
