import {useCallback} from 'react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useInfiniteScroll} from '@/common/hooks/useInfiniteScroll';
import {
    fetchShoppingListPage,
    selectShoppingListHasMore,
    selectShoppingListItems,
    selectShoppingListPage,
    selectShoppingListStatus,
} from '../shoppingListSlice';
import {ShoppingListItemRow} from './ShoppingListItemRow';
import {Loader2} from 'lucide-react';

export function ShoppingListView() {
    const dispatch = useAppDispatch();
    const items = useAppSelector(selectShoppingListItems);
    const hasMore = useAppSelector(selectShoppingListHasMore);
    const status = useAppSelector(selectShoppingListStatus);
    const currentPage = useAppSelector(selectShoppingListPage);

    const loadMore = useCallback(() => {
        dispatch(fetchShoppingListPage(currentPage + 1));
    }, [dispatch, currentPage]);

    const {sentinelRef} = useInfiniteScroll({
        hasMore,
        isLoading: status === 'loading',
        onLoadMore: loadMore,
    });

    if (Object.keys(items).length === 0 && status !== 'loading') {
        return (
            <div className="py-16 text-center text-gray-500">
                <p className="text-lg">No items yet</p>
                <p className="mt-1 text-sm">Add something to your shopping list above</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-2">
            {Object.values(items).map((item) => (
                <ShoppingListItemRow key={item.id} item={item}/>
            ))}
            <div ref={sentinelRef}/>
            {status === 'loading' && (
                <div className="flex justify-center py-4">
                    <Loader2 size={24} className="animate-spin text-gray-500"/>
                </div>
            )}
        </div>
    );
}
