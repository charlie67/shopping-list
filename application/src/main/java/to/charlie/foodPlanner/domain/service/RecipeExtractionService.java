package to.charlie.foodPlanner.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

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

	/**
	 * Extracts with one named extractor and nothing else. Where {@link #extractRecipe} keeps trying
	 * until something works, a caller who asked for a method gets that method or an error — falling
	 * back would hand them a recipe read by an extractor they explicitly did not pick.
	 */
	public ExtractedRecipe extractRecipeUsing(final Document document, final String url,
	                                          final ExtractionMethod extractionMethod)
					throws RecipeExtractionFailed {

		final RecipeExtractor recipeExtractor = orderedRecipeExtractors.stream()
						.filter(extractor -> extractionMethod == extractor.getExtractionMethod())
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException(
										"No recipe extractor for method " + extractionMethod));

		try {
			return recipeExtractor.extract(document, url);
		} catch (final Exception e) {
			// Not every extractor signals failure with RecipeExtractionFailed - JustTheRecipe throws a
			// runtime exception from its client - so anything coming out of here is a failed extraction.
			log.info("Unable to extract recipe using the requested method {} from {}", extractionMethod, url, e);
			throw new RecipeExtractionFailed(
							"Unable to extract recipe from " + url + " using " + extractionMethod.getName());
		}
	}
}


