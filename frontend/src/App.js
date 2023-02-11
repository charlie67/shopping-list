import './App.css';
import './bootstrap.min.css';

import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./pages/Layout";
import HomePage from "./pages/HomePage";
import RecipePage from "./pages/RecipePage";

function App() {
  return (
    <div className="app">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="recipes" element={<RecipePage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
);
}

export default App;
