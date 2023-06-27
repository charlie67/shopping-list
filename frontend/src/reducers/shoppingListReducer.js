import {
    FETCH_SHOPPING_LIST_ITEMS,
    SHOPPING_LIST_ITEM_CREATED,
    SHOPPING_LIST_ITEM_UPDATED
} from "../actionTypes/actionTypes";
import {createSlice} from "@reduxjs/toolkit";

const initialState = {
    shoppingListItems: [],
    hasMore: true
};

const shoppingListReducer = createSlice({
    name: 'shoppingList',
    initialState,
    reducers: {
        fetchShoppingListItemsSuccess(state, action) {
            state.shoppingListItems.pushAll(action.payload);
        },
        createShoppingListItem(state, action) {
            state.shoppingListItems.push(action.payload);
        },
        updateShoppingListItem(state, action) {
            const updatedItem = action.payload;
            const index = state.shoppingListItems.findIndex(item => item.id === updatedItem.id);
            if (index !== -1) {
                state.shoppingListItems[index] = updatedItem;
            }
        }
    }
});

export const { fetchShoppingListItemsSuccess, createShoppingListItem, updateShoppingListItem } = shoppingListReducer.actions;

export default shoppingListReducer.reducer;