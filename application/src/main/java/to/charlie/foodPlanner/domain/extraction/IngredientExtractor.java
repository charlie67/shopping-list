package to.charlie.foodPlanner.domain.extraction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;

@RequiredArgsConstructor
@Component
public class IngredientExtractor {

  private final QuantityExtractor quantityExtractor;

  public ExtractedRecipeIngredient convertIngredient(String ingredient) {
    IngredientMeasurement ingredientMeasurement = quantityExtractor.extractQuantityFromIngredient(
        ingredient);

    String wholeText = ingredient.trim().replaceAll(" +", " ");
    String ingredientName = wholeText.replace(ingredientMeasurement.quantityUnitText(), "").trim();

    return ExtractedRecipeIngredient.builder().wholeText(wholeText)
        .ingredientName(ingredientName)
        .quantity(ingredientMeasurement.quantity())
        .unit(ingredientMeasurement.unit()).build();
  }
}
