package to.charlie.foodPlanner.domain.extraction.manual;

import java.util.List;
import lombok.AllArgsConstructor;
import org.jsoup.nodes.Document;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedIngredient;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedRecipeStep;
import to.charlie.foodPlanner.domain.extraction.manual.ingredient.BaseIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.manual.recipe.BaseRecipeExtractor;

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
