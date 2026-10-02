package to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.DataDto;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ShoppingListItemsCreatedDto extends DataDto {

	private List<ShoppingListItemDto> items;
}
