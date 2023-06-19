package to.charlie.foodPlanner.domain.model.dto.shoppingList;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.dto.websocket.DataDto;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ShoppingListItemDto extends DataDto {

  private UUID id;

  private String title;

  private boolean completed;

  private int quantity;

  private LocalDateTime createdAtTime;

  private LocalDateTime updatedAtTime;
}
