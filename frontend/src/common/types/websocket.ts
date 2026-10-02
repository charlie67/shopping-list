import type {ShoppingListItemDto} from './shopping-list';

export enum WebSocketMessageType {
    SHOPPING_LIST_ITEM_CREATED = 'SHOPPING_LIST_ITEM_CREATED',
    SHOPPING_LIST_ITEM_UPDATED = 'SHOPPING_LIST_ITEM_UPDATED',
    SHOPPING_LIST_ITEM_DELETED = 'SHOPPING_LIST_ITEM_DELETED',
    SHOPPING_LIST_ITEMS_CREATED = 'SHOPPING_LIST_ITEMS_CREATED',
    PLAN_UPDATED = 'PLAN_UPDATED',
}

export interface WebSocketItemMessage {
    messageType:
        | WebSocketMessageType.SHOPPING_LIST_ITEM_CREATED
        | WebSocketMessageType.SHOPPING_LIST_ITEM_UPDATED;
    data: ShoppingListItemDto;
}

export interface WebSocketDeleteMessage {
    messageType: WebSocketMessageType.SHOPPING_LIST_ITEM_DELETED;
    data: { id: string };
}

// A whole batch of items in one frame, so adding a recipe's ingredients broadcasts once.
export interface WebSocketItemsMessage {
    messageType: WebSocketMessageType.SHOPPING_LIST_ITEMS_CREATED;
    data: { items: ShoppingListItemDto[] };
}

/**
 * One coarse event naming the weeks that changed, rather than per-meal events. Replaying placement
 * semantics — cascades, spare counts, which week a row belongs to — in reducers would duplicate
 * logic the server owns, and a week is one cheap request.
 */
export interface WebSocketPlanMessage {
    messageType: WebSocketMessageType.PLAN_UPDATED;
    data: { weekStarts: string[] };
}

export type WebSocketMessage =
    | WebSocketItemMessage
    | WebSocketItemsMessage
    | WebSocketDeleteMessage
    | WebSocketPlanMessage;
