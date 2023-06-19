package to.charlie.foodPlanner.domain.dal.mapping;

import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

public class ShoppingListItemEntityConverter {

  public static ShoppingListItemDto convertToDto(
      final ShoppingListItemEntity shoppingListItemEntity) {
    return ShoppingListItemDto.builder()
        .id(shoppingListItemEntity.getId())
        .title(shoppingListItemEntity.getTitle())
        .completed(shoppingListItemEntity.isCompleted())
        .createdAtTime(shoppingListItemEntity.getCreatedAtTime())
        .updatedAtTime(shoppingListItemEntity.getUpdatedAtTime())
        .build();
  }
}
