import {combineReducers} from '@reduxjs/toolkit';
import shoppingListReducer from '@/features/shopping-list/shoppingListSlice';
import recipesReducer from '@/features/recipes/recipesSlice';

const rootReducer = combineReducers({
    shoppingList: shoppingListReducer,
    recipes: recipesReducer,
});

export default rootReducer;
