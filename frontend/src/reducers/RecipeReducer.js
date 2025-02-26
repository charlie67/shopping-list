import {createSlice} from "@reduxjs/toolkit";
import {fetchRecipes} from "../actionTypes/RecipeActions";

const initialState = {
  recipes: [],
  hasMore: true
};

const RecipeSlice = createSlice({
  name: "recipe",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(fetchRecipes.fulfilled, (state, action) => {
      return {
        ...state,
        recipes: [...state.recipes, ...action.payload.content],
        hasMore: !action.payload.last
      }
    })
  }
});

export default RecipeSlice.reducer;