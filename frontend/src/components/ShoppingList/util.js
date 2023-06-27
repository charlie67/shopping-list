export const mapIncomingShoppingListItem = (item) => {
    item.createdAtTime = new Date(item.createdAtTime);
    item.updatedAtTime = new Date(item.updatedAtTime);
    return item;
}