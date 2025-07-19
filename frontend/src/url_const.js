export const BASE_PATH = window.location.protocol + "//" + window.location.host + "/";
export const API_BASE = BASE_PATH + "api/";

export const WEBSOCKET_URL = (window.location.protocol === "https:" ? "wss://" : "ws://") + window.location.host + "/api/wsUpdate";

export const SHOPPING_LIST_BASE = API_BASE + "shoppinglist/";
export const SHOPPING_LIST_ADD_ITEM_ENDPOINT = API_BASE + "shoppinglist";
export const SHOPPINGLIST_PAGEABLE_ENDPOINT = SHOPPING_LIST_BASE + "pageable/";

export const RECIPE_BASE = API_BASE + "recipe";
export const EXTRACT_RECIPE_ENDPOINT = RECIPE_BASE + "/extract";
export const RECIPE_PAGEABLE_ENDPOINT = RECIPE_BASE;
