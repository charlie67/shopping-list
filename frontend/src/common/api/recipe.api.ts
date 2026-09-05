import type {Page} from '../types/api';
import type {ExtractedRecipeDto, ExtractionMethod} from '../types/recipe';
import {RECIPE_ENDPOINT, RECIPE_EXTRACT_ENDPOINT} from '../constants';
import {apiDelete, apiGet, apiPatch, apiPost} from './client';

export function getRecipePage(page: number): Promise<Page<ExtractedRecipeDto>> {
    return apiGet<Page<ExtractedRecipeDto>>(`${RECIPE_ENDPOINT}?page=${page}`);
}

export function getRecipeById(id: string): Promise<ExtractedRecipeDto> {
    return apiGet<ExtractedRecipeDto>(`${RECIPE_ENDPOINT}/${id}`);
}

// Extraction only scrapes the page; the recipe is not stored until it is posted back by saveRecipe.
// Naming a method uses only that extractor: the request fails rather than falling back to another one.
export function extractRecipe(
    url: string,
    extractionMethod?: ExtractionMethod,
): Promise<ExtractedRecipeDto> {
    const method = extractionMethod
        ? `&extractionMethod=${encodeURIComponent(extractionMethod)}`
        : '';

    return apiGet<ExtractedRecipeDto>(
        `${RECIPE_EXTRACT_ENDPOINT}?url=${encodeURIComponent(url)}${method}`,
    );
}

export function saveRecipe(recipe: ExtractedRecipeDto): Promise<ExtractedRecipeDto> {
    return apiPost<ExtractedRecipeDto>(RECIPE_ENDPOINT, recipe);
}

export function updateRecipe(
    id: string,
    recipe: Omit<ExtractedRecipeDto, 'id'>,
): Promise<ExtractedRecipeDto> {
    return apiPatch<ExtractedRecipeDto>(`${RECIPE_ENDPOINT}/${id}`, recipe);
}

export function deleteRecipe(id: string): Promise<void> {
    return apiDelete(`${RECIPE_ENDPOINT}/${id}`);
}
