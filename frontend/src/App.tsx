import {BrowserRouter, Route, Routes} from 'react-router-dom';
import {RootLayout} from '@/layout/RootLayout';
import {ShoppingListPage} from '@/features/shopping-list/ShoppingListPage';
import {RecipesPage} from '@/features/recipes/RecipesPage';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<RootLayout/>}>
                    <Route index element={<ShoppingListPage/>}/>
                    <Route path="recipes" element={<RecipesPage/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
