import './App.css';
import './bootstrap.min.css';

import {BrowserRouter, Route, Routes} from "react-router-dom";
import Layout from "./pages/Layout";
import HomePage from "./pages/HomePage";
import RecipePage from "./pages/RecipePage";
import {useEffect} from "react";
import {fetchRecipes} from "./actionTypes/RecipeActions";
import {useDispatch} from "react-redux";
import {fetchShoppingList} from "./actionTypes/actions";

function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    // Dispatch the event once when the site is loaded for the first time
    dispatch(fetchRecipes(0));
    dispatch(fetchShoppingList(0));
  }, [dispatch]);

  return (
    <div className="app  bg-gray-900">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout/>}>
            <Route index element={<HomePage/>}/>
          </Route>
          <Route path="/recipes" element={<Layout/>}>
            <Route index element={<RecipePage/>}/>
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
