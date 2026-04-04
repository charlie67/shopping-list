import type {ShoppingListItemDto} from './shopping-list';

export enum WebSocketMessageType {
    SHOPPING_LIST_ITEM_CREATED = 'SHOPPING_LIST_ITEM_CREATED',
    SHOPPING_LIST_ITEM_UPDATED = 'SHOPPING_LIST_ITEM_UPDATED',
    SHOPPING_LIST_ITEM_DELETED = 'SHOPPING_LIST_ITEM_DELETED',
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

export type WebSocketMessage = WebSocketItemMessage | WebSocketDeleteMessage;
