package to.charlie.foodPlanner.domain.model.dto.shoppingList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.dto.websocket.DataDto;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ShoppingListItemDto extends DataDto {

	private UUID id;

	private String title;

	private boolean completed;

	private int quantity;

	private long createdAtTime;

	private long updatedAtTime;
}
