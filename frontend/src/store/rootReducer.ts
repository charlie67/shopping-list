import {combineReducers} from '@reduxjs/toolkit';
import shoppingListReducer from '@/features/shopping-list/shoppingListSlice';
import recipesReducer from '@/features/recipes/recipesSlice';
import planReducer from '@/features/plan/planSlice';

const rootReducer = combineReducers({
    shoppingList: shoppingListReducer,
    recipes: recipesReducer,
    plan: planReducer,
});

export default rootReducer;
