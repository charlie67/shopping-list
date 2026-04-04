export interface ShoppingListItemDto {
    id: string;
    title: string;
    completed: boolean;
    quantity: number;
    createdAtTime: number;
    updatedAtTime: number;
}

export interface ShoppingListItemCreateDto {
    title: string;
}

export interface ShoppingListItemUpdateDto {
    title?: string;
    complete?: boolean;
}
