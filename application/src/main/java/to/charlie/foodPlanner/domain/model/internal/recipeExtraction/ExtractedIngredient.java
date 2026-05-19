package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractedIngredient {

  private String name;
}
