import './App.css';
import './bootstrap.min.css';

import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./pages/Layout";
import HomePage from "./pages/HomePage";
import {RecipeList} from "./components/RecipeList/RecipeList";
import { GlobalStyles } from './global';
import { ThemeProvider } from 'styled-components';
import { theme } from './theme';

function App() {
  return (
  <ThemeProvider theme={theme}>
    <GlobalStyles />
    <div className="app">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
          </Route>
            <Route path="/recipes" element={<Layout />}>
                <Route index element={<RecipeList />} />
            </Route>
        </Routes>
      </BrowserRouter>
    </div>
  </ThemeProvider>
);
}

export default App;
