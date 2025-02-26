import {combineReducers} from 'redux';
import shoppingListReducer from "./shoppingListReducer";
import RecipeReducer from "./RecipeReducer";

const rootReducer = combineReducers({
  shoppingList: shoppingListReducer,
  recipes: RecipeReducer
});

export default rootReducer;