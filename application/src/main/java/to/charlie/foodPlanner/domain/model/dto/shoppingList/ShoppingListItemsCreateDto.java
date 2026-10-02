package to.charlie.foodPlanner.domain.model.dto.shoppingList;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingListItemsCreateDto {

	@NotEmpty(message = "Titles are required")
	private List<String> titles;
}
