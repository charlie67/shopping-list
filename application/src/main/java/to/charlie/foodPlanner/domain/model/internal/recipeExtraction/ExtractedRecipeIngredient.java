package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractedRecipeIngredient {
  private String wholeText;
  private String ingredientName;
  private Double quantity;
  private String unitText;
  private MeasurementUnit unit;
}
