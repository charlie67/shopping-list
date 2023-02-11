package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public final class ExtractedIngredient implements ExtractedItem {

  private final String originalText;
  private final String modifiedText;
  private final String tag;
  private final IngredientMeasurement quantity;
  private final String ingredientName;
  private boolean possibleDuplicate;
}
