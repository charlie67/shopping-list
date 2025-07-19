import {createSlice} from "@reduxjs/toolkit";
import {fetchShoppingList} from "../actionTypes/actions";
import {
  SHOPPING_LIST_ITEM_CREATED,
  SHOPPING_LIST_ITEM_DELETED,
  SHOPPING_LIST_ITEM_UPDATED
} from "../actionTypes/actionTypes";

const initialState = {
  shoppingListItems: [],
  hasMore: true
};


const shoppingListSlice = createSlice({
  name: "shoppingList",
  initialState,
  reducers: {
    createShoppingListItem(state, action) {
      state.shoppingListItems.push(action.payload);
    },
  },
  extraReducers: (builder) => {
    builder.addCase(fetchShoppingList.fulfilled, (state, action) => {
      state.hasMore = !action.payload.last;
      state.shoppingListItems.push(...action.payload.content);
      reorderShoppingList(state.shoppingListItems)
    }).addCase(SHOPPING_LIST_ITEM_UPDATED, (state, action) => {
      const index = state.shoppingListItems.findIndex(item => item.id === action.payload.id);
      if (index !== -1) {
        state.shoppingListItems[index] = action.payload;
        reorderShoppingList(state.shoppingListItems)
      }
    }).addCase(SHOPPING_LIST_ITEM_CREATED, (state, action) => {
      const item = action.payload;
      console.log("Adding item to shopping list:", item);
      console.log("Item already exists:", state.shoppingListItems.find(it => it.id === item.id));

      if (state.shoppingListItems.find(it => it.id === item.id) === undefined) {
        state.shoppingListItems.push(item);
        reorderShoppingList(state.shoppingListItems)
      }
    }).addCase(SHOPPING_LIST_ITEM_DELETED, (state, action) => {
      const index = state.shoppingListItems.findIndex(item => item.id === action.payload.id);
      if (index !== -1) {
        state.shoppingListItems.splice(index, 1);
      }
    })
  },
});

const reorderShoppingList = (shoppingList) => {
  const items = shoppingList;
  for (let i = 0; i < items.length; i++) {
    for (let j = i + 1; j < items.length; j++) {
      if (compareItems(items[j], items[i]) < 0) {
        [items[i], items[j]] = [items[j], items[i]];
      }
    }
  }
  return items;
}

const compareItems = (a, b) => {
  if (a.completed !== b.completed) {
    return a.completed ? 1 : -1;
  }

  if (a.completed) {
    return b.updatedAtTime - a.updatedAtTime;
  } else {
    return b.createdAtTime - a.createdAtTime;
  }
}

export const {
  createShoppingListItem,
  updateShoppingListItem,
} = shoppingListSlice.actions;

export default shoppingListSlice.reducer;