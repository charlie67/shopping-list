package to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JustTheRecipeResponseDto(
				String version,
				String id,
				String name,
				String sourceUrl,
				Integer servings,
				Long totalTime,
				List<String> categories,
				List<String> imageUrls,
				List<String> keywords,
				List<IngredientDto> ingredients,
				List<InstructionDto> instructions,
				String source
) {

}