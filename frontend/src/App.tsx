import {BrowserRouter, Route, Routes} from 'react-router-dom';
import {RootLayout} from '@/layout/RootLayout';
import {ShoppingListPage} from '@/features/shopping-list/ShoppingListPage';
import {RecipesPage} from '@/features/recipes/RecipesPage';
import {ToCookPage} from '@/features/plan/ToCookPage';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<RootLayout/>}>
                    <Route index element={<ShoppingListPage/>}/>
                    <Route path="recipes" element={<RecipesPage/>}/>
                    <Route path="to-cook" element={<ToCookPage/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
