import type {Page} from '../types/api';
import type {ExtractedRecipeDto} from '../types/recipe';
import {RECIPE_ENDPOINT, RECIPE_EXTRACT_ENDPOINT} from '../constants';
import {apiGet, apiPost} from './client';

export function getRecipePage(page: number): Promise<Page<ExtractedRecipeDto>> {
    return apiGet<Page<ExtractedRecipeDto>>(`${RECIPE_ENDPOINT}?page=${page}`);
}

export function extractRecipe(url: string, save: boolean): Promise<ExtractedRecipeDto> {
    return apiPost<ExtractedRecipeDto>(
        `${RECIPE_EXTRACT_ENDPOINT}?url=${encodeURIComponent(url)}&save=${save}`,
    );
}
