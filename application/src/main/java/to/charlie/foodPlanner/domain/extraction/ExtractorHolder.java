package to.charlie.foodPlanner.domain.extraction;

import java.util.List;
import lombok.AllArgsConstructor;
import org.jsoup.nodes.Document;
import to.charlie.foodPlanner.domain.extraction.ingredient.BaseIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.BaseRecipeExtractor;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeStep;

@AllArgsConstructor
public class ExtractorHolder {

  BaseRecipeExtractor recipeExtractor;

  BaseIngredientExtractor ingredientExtractor;

  public List<ExtractedIngredient> extractIngredients(Document document) {
    return ingredientExtractor.extractIngredients(document);
  }

  public List<ExtractedRecipeStep> extractRecipeSteps(Document document) {
    return recipeExtractor.extractRecipeSteps(document);
  }
}
