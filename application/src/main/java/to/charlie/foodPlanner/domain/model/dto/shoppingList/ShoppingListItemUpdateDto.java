package to.charlie.foodPlanner.domain.model.dto.shoppingList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShoppingListItemUpdateDto {

  private String title;
  private Boolean complete;
}
