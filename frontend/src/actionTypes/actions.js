import {
    SHOPPING_LIST_ITEM_CREATED,
    SHOPPING_LIST_ITEM_DELETED,
    SHOPPING_LIST_ITEM_UPDATED
} from "./actionTypes";
import {SHOPPINGLIST_PAGEABLE_ENDPOINT} from "../url_const";
import {createAsyncThunk} from '@reduxjs/toolkit';

export const fetchShoppingList = createAsyncThunk(
    'shoppingList/fetchShoppingList',
    async (page, { dispatch, rejectWithValue }) => {
        try {
            console.log("fetching shopping list")
            const response = await fetch(SHOPPINGLIST_PAGEABLE_ENDPOINT + page);
            const data = await response.json();
            console.log("data", data)
            return data;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const shoppingListItemCreated = (item) => ({
    type: SHOPPING_LIST_ITEM_CREATED,
    payload: item
});

export const shoppingListItemUpdated = (item) => ({
    type: SHOPPING_LIST_ITEM_UPDATED,
    payload: item
});

export const shoppingListItemDeleted = (item) => ({
    type: SHOPPING_LIST_ITEM_DELETED,
    payload: item
});