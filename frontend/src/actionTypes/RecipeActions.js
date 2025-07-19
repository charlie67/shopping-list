import {createAsyncThunk} from "@reduxjs/toolkit";
import {RECIPE_PAGEABLE_ENDPOINT} from "../url_const";

export const fetchRecipes = createAsyncThunk(
  'recipe/fetchRecipes',
  async (page, {dispatch, rejectWithValue}) => {
    try {
      const url = new URL(RECIPE_PAGEABLE_ENDPOINT);
      url.searchParams.append("page", page);
      const response = await fetch(url);
      return await response.json();
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);