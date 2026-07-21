import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import type {ExtractedRecipeDto} from '@/common/types/recipe';
import * as api from '@/common/api/recipe.api';
import type {RootState} from '@/store/store';

interface RecipesState {
    items: Record<string, ExtractedRecipeDto>;
    // The recipe just extracted from a URL, held for review in the editor until it is saved or discarded.
    draft: ExtractedRecipeDto | null;
    hasMore: boolean;
    currentPage: number;
    status: 'idle' | 'loading' | 'failed';
    extractionStatus: 'idle' | 'loading' | 'succeeded' | 'failed';
    saveStatus: 'idle' | 'loading' | 'failed';
    updateStatus: 'idle' | 'loading' | 'failed';
    deleteStatus: 'idle' | 'loading' | 'failed';
}

const initialState: RecipesState = {
    items: {},
    draft: null,
    hasMore: true,
    currentPage: -1,
    status: 'idle',
    extractionStatus: 'idle',
    saveStatus: 'idle',
    updateStatus: 'idle',
    deleteStatus: 'idle',
};

export const fetchRecipesPage = createAsyncThunk(
    'recipes/fetchPage',
    async (page: number) => {
        return api.getRecipePage(page);
    },
);

export const fetchRecipeById = createAsyncThunk(
    'recipes/fetchById',
    async (id: string) => {
        return api.getRecipeById(id);
    },
);

export const extractRecipeFromUrl = createAsyncThunk(
    'recipes/extract',
    async (url: string) => {
        return api.extractRecipe(url);
    },
);

export const saveRecipe = createAsyncThunk(
    'recipes/save',
    async (recipe: ExtractedRecipeDto) => {
        return api.saveRecipe(recipe);
    },
);

// The id travels in the path, so it is stripped out of the body the rest of the recipe is sent in.
export const updateRecipe = createAsyncThunk(
    'recipes/update',
    async ({id, recipe}: { id: string; recipe: ExtractedRecipeDto }) => {
        const {id: _ignored, ...body} = recipe;
        return api.updateRecipe(id, body);
    },
);

export const deleteRecipe = createAsyncThunk(
    'recipes/delete',
    async (id: string) => {
        await api.deleteRecipe(id);
        return id;
    },
);

const recipesSlice = createSlice({
    name: 'recipes',
    initialState,
    reducers: {
        clearDraft: (state) => {
            state.draft = null;
            state.saveStatus = 'idle';
        },
        clearUpdateStatus: (state) => {
            state.updateStatus = 'idle';
        },
        clearDeleteStatus: (state) => {
            state.deleteStatus = 'idle';
        },
    },
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
            .addCase(fetchRecipeById.fulfilled, (state, action) => {
                if (action.payload) {
                    state.items[action.payload.id] = action.payload;
                }
            })
            .addCase(extractRecipeFromUrl.pending, (state) => {
                state.extractionStatus = 'loading';
            })
            // An extracted recipe only has an id when it was already in the database, in which case it
            // goes straight into the grid; anything else is a draft awaiting review in the editor.
            .addCase(extractRecipeFromUrl.fulfilled, (state, action) => {
                state.extractionStatus = 'succeeded';
                if (!action.payload) {
                    return;
                }
                if (action.payload.id) {
                    state.items[action.payload.id] = action.payload;
                } else {
                    state.draft = action.payload;
                }
            })
            .addCase(extractRecipeFromUrl.rejected, (state) => {
                state.extractionStatus = 'failed';
            })
            .addCase(saveRecipe.pending, (state) => {
                state.saveStatus = 'loading';
            })
            .addCase(saveRecipe.fulfilled, (state, action) => {
                state.saveStatus = 'idle';
                state.draft = null;
                if (action.payload) {
                    state.items[action.payload.id] = action.payload;
                }
            })
            .addCase(saveRecipe.rejected, (state) => {
                state.saveStatus = 'failed';
            })
            .addCase(updateRecipe.pending, (state) => {
                state.updateStatus = 'loading';
            })
            .addCase(updateRecipe.fulfilled, (state, action) => {
                state.updateStatus = 'idle';
                if (action.payload) {
                    state.items[action.payload.id] = action.payload;
                }
            })
            .addCase(updateRecipe.rejected, (state) => {
                state.updateStatus = 'failed';
            })
            .addCase(deleteRecipe.pending, (state) => {
                state.deleteStatus = 'loading';
            })
            // Not optimistic: the modal stays open on failure, so the recipe only leaves the grid
            // once the backend has actually deleted it.
            .addCase(deleteRecipe.fulfilled, (state, action) => {
                state.deleteStatus = 'idle';
                delete state.items[action.payload];
            })
            .addCase(deleteRecipe.rejected, (state) => {
                state.deleteStatus = 'failed';
            });
    },
});

export const selectRecipeItems = (state: RootState) => state.recipes.items;
export const selectRecipesHasMore = (state: RootState) => state.recipes.hasMore;
export const selectRecipesStatus = (state: RootState) => state.recipes.status;
export const selectRecipesPage = (state: RootState) => state.recipes.currentPage;
export const selectExtractionStatus = (state: RootState) => state.recipes.extractionStatus;
export const selectRecipeDraft = (state: RootState) => state.recipes.draft;
export const selectSaveStatus = (state: RootState) => state.recipes.saveStatus;
export const selectUpdateStatus = (state: RootState) => state.recipes.updateStatus;
export const selectDeleteStatus = (state: RootState) => state.recipes.deleteStatus;

export const {clearDraft, clearUpdateStatus, clearDeleteStatus} = recipesSlice.actions;

export default recipesSlice.reducer;
