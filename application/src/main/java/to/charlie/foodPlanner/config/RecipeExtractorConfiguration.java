package to.charlie.foodPlanner.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.JustTheRecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.JsonLdExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.microdata.MicrodataExtractor;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RecipeExtractorConfiguration {

	private final JsonLdExtractor jsonLdExtractor;
	private final MicrodataExtractor microdataExtractor;
	private final JustTheRecipeExtractor justTheRecipeExtractor;

	@Bean
	@Qualifier("orderedRecipeExtractors")
	public List<RecipeExtractor> orderedRecipeExtractors() {
		// this is the ordered list that is traversed until a recipe is successfully extracted
		return List.of(jsonLdExtractor, microdataExtractor, justTheRecipeExtractor);
	}
}
