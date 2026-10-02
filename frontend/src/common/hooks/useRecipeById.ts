import {useEffect, useRef} from 'react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import type {ExtractedRecipeDto} from '@/common/types/recipe';
import {fetchRecipeById, selectRecipeItems} from '@/features/recipes/recipesSlice';

/**
 * The recipe with this id, fetching it when it is not among the pages already loaded — after a
 * refresh onto a URL that names one, or from the planner, which holds recipe ids rather than
 * recipes. Returns undefined until it arrives. The ref stops a failed fetch from looping.
 */
export function useRecipeById(id: string | null): ExtractedRecipeDto | undefined {
    const dispatch = useAppDispatch();
    const items = useAppSelector(selectRecipeItems);
    const requestedIdRef = useRef<string | null>(null);

    useEffect(() => {
        if (id && !items[id] && requestedIdRef.current !== id) {
            requestedIdRef.current = id;
            dispatch(fetchRecipeById(id));
        }
    }, [dispatch, id, items]);

    return id ? items[id] : undefined;
}
