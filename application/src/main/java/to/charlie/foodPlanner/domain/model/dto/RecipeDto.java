package to.charlie.foodPlanner.domain.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RecipeDto {

  private UUID id;

  @NotEmpty(message = "Title is required")
  private String title;

  private String url;

  private String steps;

  private Set<IngredientDto> ingredients;
}
