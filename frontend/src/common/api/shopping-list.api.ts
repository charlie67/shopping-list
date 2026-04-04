import type {Page} from '../types/api';
import type {ShoppingListItemDto} from '../types/shopping-list';
import {SHOPPING_LIST_ENDPOINT, SHOPPING_LIST_PAGEABLE_ENDPOINT} from '../constants';
import {apiDelete, apiGet, apiPatch, apiPost} from './client';

export function getShoppingListPage(page: number): Promise<Page<ShoppingListItemDto>> {
    return apiGet<Page<ShoppingListItemDto>>(`${SHOPPING_LIST_PAGEABLE_ENDPOINT}${page}`);
}

export function createShoppingListItem(title: string): Promise<ShoppingListItemDto> {
    return apiPost<ShoppingListItemDto>(SHOPPING_LIST_ENDPOINT, {title});
}

export function updateShoppingListItem(
    id: string,
    updates: { title?: string; complete?: boolean },
): Promise<ShoppingListItemDto> {
    return apiPatch<ShoppingListItemDto>(`${SHOPPING_LIST_ENDPOINT}/${id}`, updates);
}

export function deleteShoppingListItem(id: string): Promise<void> {
    return apiDelete(`${SHOPPING_LIST_ENDPOINT}/${id}`);
}
