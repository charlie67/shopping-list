const BASE_PATH = `${window.location.protocol}//${window.location.host}/`;
export const API_BASE = `${BASE_PATH}api/`;

export const WEBSOCKET_URL =
    `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/wsUpdate`;

export const SHOPPING_LIST_ENDPOINT = `${API_BASE}shoppinglist`;
export const SHOPPING_LIST_PAGEABLE_ENDPOINT = `${API_BASE}shoppinglist/pageable/`;
export const RECIPE_ENDPOINT = `${API_BASE}recipe`;
export const RECIPE_EXTRACT_ENDPOINT = `${API_BASE}recipe/extract`;
