import {configureStore} from "@reduxjs/toolkit";
import rootReducer from "./reducers/reducers";

const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these paths in the state
        ignoredPaths: ['shoppingListItems[].completedAtTime', 'shoppingListItems[].updatedAtTime'],
      },
    }),
});


export default store;