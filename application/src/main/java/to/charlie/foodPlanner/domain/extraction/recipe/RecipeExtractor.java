package to.charlie.foodPlanner.domain.extraction.recipe;

import org.jsoup.nodes.Document;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

public interface RecipeExtractor {

	ExtractedRecipe extract(Document document, String url) throws RecipeExtractionFailed;
}
