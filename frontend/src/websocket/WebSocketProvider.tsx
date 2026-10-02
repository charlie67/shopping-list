import {useEffect} from 'react';
import useWebSocket from 'react-use-websocket';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {WEBSOCKET_URL} from '@/common/constants';
import type {WebSocketMessage} from '@/common/types/websocket';
import {WebSocketMessageType} from '@/common/types/websocket';
import type {ShoppingListItemDto} from '@/common/types/shopping-list';
import {
    shoppingListItemCreated,
    shoppingListItemDeleted,
    shoppingListItemsCreated,
    shoppingListItemUpdated,
} from '@/features/shopping-list/shoppingListSlice';
import {planWeeksInvalidated, selectPlanLastMutationAt} from '@/features/plan/planSlice';

// sendMessageToAllClients has no sender identity, so this client also hears the echo of its own
// write. Ignoring plan updates for a moment after mutating stops a refetch landing mid-gesture.
const OWN_ECHO_WINDOW_MS = 1500;

export function WebSocketProvider({children}: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const lastMutationAt = useAppSelector(selectPlanLastMutationAt);
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
            case WebSocketMessageType.SHOPPING_LIST_ITEMS_CREATED:
                dispatch(shoppingListItemsCreated(
                    lastJsonMessage.data as { items: ShoppingListItemDto[] }));
                break;
            case WebSocketMessageType.SHOPPING_LIST_ITEM_UPDATED:
                dispatch(shoppingListItemUpdated(lastJsonMessage.data as ShoppingListItemDto));
                break;
            case WebSocketMessageType.SHOPPING_LIST_ITEM_DELETED:
                dispatch(shoppingListItemDeleted(lastJsonMessage.data as { id: string }));
                break;
            case WebSocketMessageType.PLAN_UPDATED: {
                if (Date.now() - lastMutationAt < OWN_ECHO_WINDOW_MS) break;
                const {weekStarts} = lastJsonMessage.data as { weekStarts: string[] };
                // Only drop what we hold. The planner refetches whichever week is on screen the
                // moment it finds it missing, and the rest are read again when they are visited.
                dispatch(planWeeksInvalidated({weekStarts}));
                break;
            }
        }
    }, [lastJsonMessage, dispatch, lastMutationAt]);

    return <>{children}</>;
}
