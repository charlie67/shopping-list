import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import type {ExtractedRecipeDto} from '@/common/types/recipe';
import * as api from '@/common/api/recipe.api';
import type {RootState} from '@/store/store';

interface RecipesState {
    items: Record<string, ExtractedRecipeDto>;
    hasMore: boolean;
    currentPage: number;
    status: 'idle' | 'loading' | 'failed';
    extractionStatus: 'idle' | 'loading' | 'succeeded' | 'failed';
}

const initialState: RecipesState = {
    items: {},
    hasMore: true,
    currentPage: -1,
    status: 'idle',
    extractionStatus: 'idle',
};

export const fetchRecipesPage = createAsyncThunk(
    'recipes/fetchPage',
    async (page: number) => {
        return api.getRecipePage(page);
    },
);

export const extractRecipeFromUrl = createAsyncThunk(
    'recipes/extract',
    async (url: string) => {
        return api.extractRecipe(url, true);
    },
);

const recipesSlice = createSlice({
    name: 'recipes',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchRecipesPage.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(fetchRecipesPage.fulfilled, (state, action) => {
                state.status = 'idle';
                state.hasMore = !action.payload.last;
                state.currentPage = action.payload.number;
                for (const item of action.payload.content) {
                    state.items[item.id] = item;
                }
            })
            .addCase(fetchRecipesPage.rejected, (state) => {
                state.status = 'failed';
            })
            .addCase(extractRecipeFromUrl.pending, (state) => {
                state.extractionStatus = 'loading';
            })
            .addCase(extractRecipeFromUrl.fulfilled, (state, action) => {
                state.extractionStatus = 'succeeded';
                if (action.payload) {
                    state.items[action.payload.id] = action.payload;
                }
            })
            .addCase(extractRecipeFromUrl.rejected, (state) => {
                state.extractionStatus = 'failed';
            });
    },
});

export const selectRecipeItems = (state: RootState) => state.recipes.items;
export const selectRecipesHasMore = (state: RootState) => state.recipes.hasMore;
export const selectRecipesStatus = (state: RootState) => state.recipes.status;
export const selectRecipesPage = (state: RootState) => state.recipes.currentPage;
export const selectExtractionStatus = (state: RootState) => state.recipes.extractionStatus;

export default recipesSlice.reducer;
