package to.charlie.foodPlanner.domain.extraction.recipe;

import org.jsoup.nodes.Document;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

public interface RecipeExtractor {

	ExtractedRecipe extract(Document document, String url) throws RecipeExtractionFailed;

	/**
	 * The method this extractor reads a page with, and the one it stamps on the recipes it produces.
	 * Lets a caller ask for a single extractor by name instead of walking the whole chain.
	 */
	ExtractionMethod getExtractionMethod();
}
