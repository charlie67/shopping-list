import {useEffect} from 'react';
import useWebSocket from 'react-use-websocket';
import {useAppDispatch} from '@/common/hooks/redux';
import {WEBSOCKET_URL} from '@/common/constants';
import type {WebSocketMessage} from '@/common/types/websocket';
import {WebSocketMessageType} from '@/common/types/websocket';
import type {ShoppingListItemDto} from '@/common/types/shopping-list';
import {
    shoppingListItemCreated,
    shoppingListItemDeleted,
    shoppingListItemUpdated,
} from '@/features/shopping-list/shoppingListSlice';

export function WebSocketProvider({children}: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const {lastJsonMessage} = useWebSocket<WebSocketMessage>(WEBSOCKET_URL, {
        shouldReconnect: () => true,
        retryOnError: true,
        reconnectAttempts: 999999,
    });

    useEffect(() => {
        if (!lastJsonMessage) return;

        switch (lastJsonMessage.messageType) {
            case WebSocketMessageType.SHOPPING_LIST_ITEM_CREATED:
                dispatch(shoppingListItemCreated(lastJsonMessage.data as ShoppingListItemDto));
                break;
            case WebSocketMessageType.SHOPPING_LIST_ITEM_UPDATED:
                dispatch(shoppingListItemUpdated(lastJsonMessage.data as ShoppingListItemDto));
                break;
            case WebSocketMessageType.SHOPPING_LIST_ITEM_DELETED:
                dispatch(shoppingListItemDeleted(lastJsonMessage.data as { id: string }));
                break;
        }
    }, [lastJsonMessage, dispatch]);

    return <>{children}</>;
}
