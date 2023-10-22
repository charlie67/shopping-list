package to.charlie.foodPlanner.domain.extraction.manual.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;

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
