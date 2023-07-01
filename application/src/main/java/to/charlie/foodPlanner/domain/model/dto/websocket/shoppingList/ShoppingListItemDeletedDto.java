package to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import to.charlie.foodPlanner.domain.model.dto.websocket.DataDto;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ShoppingListItemDeletedDto extends DataDto {
  private UUID id;
}
