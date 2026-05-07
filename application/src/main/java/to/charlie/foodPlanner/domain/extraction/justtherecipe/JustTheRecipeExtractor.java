package to.charlie.foodPlanner.domain.extraction.justtherecipe;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.IngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.justtherecipe.dto.InstructionDto;
import to.charlie.foodPlanner.domain.extraction.justtherecipe.dto.JustTheRecipeResponseDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;
import to.charlie.foodPlanner.infrastructure.rest.clients.JustTheRecipeClient;

@Component
@RequiredArgsConstructor
public class JustTheRecipeExtractor implements RecipeExtractor {

	private final JustTheRecipeClient justTheRecipeClient;
	private final IngredientExtractor ingredientExtractor;

	@Override
	public ExtractedRecipe extract(final Document document, final String url) {

		final JustTheRecipeResponseDto response = justTheRecipeClient.getRecipe(url);

		return ExtractedRecipe.builder()
						.name(response.name())
						.url(response.sourceUrl())
						.recipeYield(String.valueOf(response.servings()))
						.totalTime(String.valueOf(response.totalTime()))
						.extractedRecipeIngredients(response.ingredients().stream()
										.map(ingredient -> ingredientExtractor.convertIngredient(ingredient.name()))
										.toList())
						.extractedRecipeInstructions(response.instructions().stream()
										.map(this::mapInstruction)
										.toList())
						.imageUrl(findMainRecipeImage(document))
						.build();
	}

	private ExtractedRecipeInstruction mapInstruction(final InstructionDto instruction) {
		return ExtractedRecipeInstruction.builder()
						.text(instruction.text())
						.type(instruction.type())
						.build();
	}

	public String findMainRecipeImage(final Document document) {
		// Look for Open Graph image
		final Element ogImage = document.selectFirst("meta[property=og:image]");
		if (ogImage != null && !ogImage.attr("content").isEmpty()) {
			return ogImage.attr("content");
		}

		// Fallback to Twitter card image
		final Element twitterImage = document.selectFirst("meta[name=twitter:image]");
		if (twitterImage != null && !twitterImage.attr("content").isEmpty()) {
			return twitterImage.attr("content");
		}

		return null;
	}

}
