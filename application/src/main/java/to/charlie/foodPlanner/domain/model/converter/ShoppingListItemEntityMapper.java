package to.charlie.foodPlanner.domain.model.converter;

import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

import java.time.ZoneOffset;

public class ShoppingListItemEntityMapper {

  public static ShoppingListItemDto entityToDto(
          final ShoppingListItemEntity shoppingListItemEntity) {
    return ShoppingListItemDto.builder()
            .id(shoppingListItemEntity.getId())
            .title(shoppingListItemEntity.getTitle())
            .completed(shoppingListItemEntity.isCompleted())
            .createdAtTime(shoppingListItemEntity.getCreatedAtTime().toEpochSecond(ZoneOffset.UTC))
            .updatedAtTime(shoppingListItemEntity.getUpdatedAtTime().toEpochSecond(ZoneOffset.UTC))
            .build();
  }
}
