package to.charlie.foodPlanner.domain.extraction;

import org.jsoup.nodes.Document;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

public interface RecipeExtractor {
  ExtractedRecipe extract(Document document);
}
