import {createAsyncThunk, createSlice, type PayloadAction} from '@reduxjs/toolkit';
import type {ShoppingListItemDto} from '@/common/types/shopping-list';
import * as api from '@/common/api/shopping-list.api';
import type {RootState} from '@/store/store';

interface ShoppingListState {
    items: Record<string, ShoppingListItemDto>;
    hasMore: boolean;
    currentPage: number;
    status: 'idle' | 'loading' | 'failed';
}

const initialState: ShoppingListState = {
    items: {} as Record<string, ShoppingListItemDto>,
    hasMore: true,
    currentPage: -1,
    status: 'idle',
};

export const fetchShoppingListPage = createAsyncThunk(
    'shoppingList/fetchPage',
    async (page: number) => {
        return api.getShoppingListPage(page);
    },
);

export const addShoppingListItem = createAsyncThunk(
    'shoppingList/addItem',
    async (title: string) => {
        return api.createShoppingListItem(title);
    },
);

export const toggleItemComplete = createAsyncThunk(
    'shoppingList/toggleComplete',
    async ({id, completed}: { id: string; completed: boolean }) => {
        return api.updateShoppingListItem(id, {complete: !completed});
    },
);

export const removeShoppingListItem = createAsyncThunk(
    'shoppingList/removeItem',
    async (item: ShoppingListItemDto) => {
        await api.deleteShoppingListItem(item.id);
        return item.id;
    },
);

function sortItems(itemsMap: Record<string, ShoppingListItemDto>): Record<string, ShoppingListItemDto> {
    const itemsArray = Object.values(itemsMap).filter(
        (item) => item && typeof item === 'object' && item.id
    );

    itemsArray.sort((a, b) => {
        if (a.completed !== b.completed) {
            return a.completed ? 1 : -1;
        }
        return (b.createdAtTime || 0) - (a.createdAtTime || 0);
    });

    const sortedMap: Record<string, ShoppingListItemDto> = {};
    for (const item of itemsArray) {
        sortedMap[item.id] = item;
    }

    return sortedMap;
}


const shoppingListSlice = createSlice({
    name: 'shoppingList',
    initialState,
    reducers: {
        shoppingListItemCreated(state, action: PayloadAction<ShoppingListItemDto>) {
            state.items[action.payload.id] = action.payload;
            state.items = sortItems(state.items);
        },
        shoppingListItemUpdated(state, action: PayloadAction<ShoppingListItemDto>) {
            state.items[action.payload.id] = action.payload;
            state.items = sortItems(state.items);
        },
        shoppingListItemDeleted(state, action: PayloadAction<{ id: string }>) {
            delete state.items[action.payload.id];
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchShoppingListPage.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(fetchShoppingListPage.fulfilled, (state, action) => {
                state.status = 'idle';
                state.hasMore = !action.payload.last;
                state.currentPage = action.payload.number;

                for (const item of action.payload.content) {
                    state.items[item.id] = item;
                }
                state.items = sortItems(state.items);
            })
            .addCase(fetchShoppingListPage.rejected, (state) => {
                state.status = 'failed';
            })
            .addCase(toggleItemComplete.pending, (state, action) => {
                const {id, completed} = action.meta.arg;
                const item = state.items[id];
                if (item) {
                    item.completed = !completed;
                    state.items = sortItems(state.items);
                }
            })
            .addCase(toggleItemComplete.fulfilled, (state, action) => {
                state.items[action.payload.id] = action.payload;
                state.items = sortItems(state.items);
            })
            .addCase(toggleItemComplete.rejected, (state, action) => {
                const {id, completed} = action.meta.arg;
                const item = state.items[id];
                if (item) {
                    item.completed = completed;
                    state.items = sortItems(state.items);
                }
            })
            .addCase(removeShoppingListItem.pending, (state, action) => {
                delete state.items[action.meta.arg.id];
            })
            .addCase(removeShoppingListItem.rejected, (state, action) => {
                const previousItem = action.meta.arg;
                state.items[previousItem.id] = previousItem;
                state.items = sortItems(state.items);
            })
            .addCase(addShoppingListItem.fulfilled, (state, action) => {
                state.items[action.payload.id] = action.payload;
                state.items = sortItems(state.items);
            });
    },
});

export const {shoppingListItemCreated, shoppingListItemUpdated, shoppingListItemDeleted} =
    shoppingListSlice.actions;

export const selectShoppingListItems = (state: RootState) => state.shoppingList.items;
export const selectShoppingListHasMore = (state: RootState) => state.shoppingList.hasMore;
export const selectShoppingListStatus = (state: RootState) => state.shoppingList.status;
export const selectShoppingListPage = (state: RootState) => state.shoppingList.currentPage;

export default shoppingListSlice.reducer;
