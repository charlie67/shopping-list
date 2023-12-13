package to.charlie.foodPlanner.domain.model.dto.recipe;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RecipeDto {

  private UUID id;

  @NotEmpty(message = "Title is required")
  private String name;

  private String url;

  @NotEmpty(message = "Steps are required")
  private String steps;

  @NotEmpty(message = "Ingredients are required")
  private Set<IngredientDto> ingredients;
}
