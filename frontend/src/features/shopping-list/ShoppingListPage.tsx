import {useEffect} from 'react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {fetchShoppingListPage, selectShoppingListPage} from './shoppingListSlice';
import {AddItemInput} from './components/AddItemInput';
import {ShoppingListView} from './components/ShoppingListView';

export function ShoppingListPage() {
    const dispatch = useAppDispatch();
    const currentPage = useAppSelector(selectShoppingListPage);

    useEffect(() => {
        if (currentPage === -1) {
            dispatch(fetchShoppingListPage(0));
        }
    }, [dispatch, currentPage]);

    return (
        <div className="mx-auto w-full max-w-2xl px-4 py-6">
            <AddItemInput/>
            <div className="mt-4">
                <ShoppingListView/>
            </div>
        </div>
    );
}
