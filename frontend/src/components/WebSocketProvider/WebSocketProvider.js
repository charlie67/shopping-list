import React, {createContext, useEffect} from 'react';
import useWebSocket from 'react-use-websocket';
import {WEBSOCKET_URL} from "../../url_const";
import {connect} from "react-redux";
import {SHOPPING_LIST_ITEM_CREATED, SHOPPING_LIST_ITEM_UPDATED} from "../../actionTypes/actionTypes";
import {shoppingListItemCreated, shoppingListItemUpdated} from "../../actionTypes/actions";
import {mapIncomingShoppingListItem} from "../ShoppingList/util";

// Create the WebSocketContext
export const WebSocketContext = createContext();

const WebSocketProvider = ({ children, dispatch }) => {
    const { lastJsonMessage } = useWebSocket(WEBSOCKET_URL, {
        onOpen: () => {
            console.debug('WebSocket connection established.');
        },
        onError: (event) => {
            console.error('WebSocket error observed:', event);
        },
        shouldReconnect: () => true
    });

    useEffect(() => {
        // Handle received messages or perform other WebSocket-related tasks here
        if (lastJsonMessage) {
            console.debug("WS message received", lastJsonMessage);

            if (lastJsonMessage.messageType === SHOPPING_LIST_ITEM_CREATED) {
               const itemCreatedMessage = mapIncomingShoppingListItem(lastJsonMessage.data);

                dispatch(shoppingListItemCreated(lastJsonMessage.data));
            } else if (lastJsonMessage.messageType === SHOPPING_LIST_ITEM_UPDATED) {
                const itemCreatedMessage = mapIncomingShoppingListItem(lastJsonMessage.data);

                dispatch(shoppingListItemUpdated(lastJsonMessage.data));
            }
        }
    }, [lastJsonMessage]);

    const websocketContextValue = {
        lastJsonMessage
    };

    return (
        <WebSocketContext.Provider value={websocketContextValue}>
            {children}
        </WebSocketContext.Provider>
    );
};

export default connect()(WebSocketProvider);