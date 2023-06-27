export const BASE_PATH = window.location.protocol + "//" + window.location.host + "/";
export const API_BASE = BASE_PATH + "api/";

export const WEBSOCKET_URL = "ws://" + window.location.host + "/api/wsUpdate";

export const SHOPPINGLIST_BASE = API_BASE + "shoppinglist/";
export const SHOPPING_LIST_ADD_ITEM_ENDPOINT = API_BASE + "shoppinglist";
export const SHOPPINGLIST_PAGEABLE_ENDPOINT = SHOPPINGLIST_BASE + "pageable/";