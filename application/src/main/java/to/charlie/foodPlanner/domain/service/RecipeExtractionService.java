package to.charlie.foodPlanner.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

import java.util.List;

@Slf4j
@Service
public class RecipeExtractionService {

	private final List<RecipeExtractor> orderedRecipeExtractors;

	public RecipeExtractionService(
					@Qualifier("orderedRecipeExtractors") final List<RecipeExtractor> orderedRecipeExtractors) {
		this.orderedRecipeExtractors = orderedRecipeExtractors;
	}

	public ExtractedRecipe extractRecipe(final Document document, final String url) {
		for (final RecipeExtractor recipeExtractor : orderedRecipeExtractors) {
			try {
				return recipeExtractor.extract(document, url);
			} catch (final RecipeExtractionFailed e) {
				log.info("Unable to extract recipe using {} from {}",
								recipeExtractor.getClass().getSimpleName(), url, e);
			} catch (final Exception e) {
				log.error("Unhandled exception throw from {} when extracting recipe {}",
								recipeExtractor.getClass().getSimpleName(), url, e);
			}
		}

		throw new IllegalArgumentException("Exhausted all recipe extractors");
	}
}


