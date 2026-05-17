package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdHowToStep;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonLdExtractorConverter implements Converter<JsonLdRecipe, ExtractedRecipe> {

	private final IngredientBreakdownService ingredientExtractor;
	private final ObjectMapper objectMapper;

	@Override
	public ExtractedRecipe convert(final JsonLdRecipe source) {
		final List<ExtractedRecipeInstruction> instructions = extractInstructions(
						source.getRecipeInstructions());

		return ExtractedRecipe.builder().url(source.getUrl())
						.name(source.getName())
						.description(source.getDescription())
						.dateModified(source.getDateModified())
						.datePublished(source.getDatePublished())
						.keywords(source.getKeywords())
						.cookTime(source.getCookTime())
						.prepTime(source.getPrepTime())
						.totalTime(source.getTotalTime())
						.recipeCategory(source.getRecipeCategory().stream().findFirst().orElse(""))// todo
						.recipeYield(source.getRecipeYield()
										.stream()
										.findFirst()
										.orElse(""))// todo map these as lists all the way down
						.extractedRecipeIngredients(
										source.getRecipeIngredients().stream()
														.flatMap(ingredient -> ingredientExtractor.convertIngredient(ingredient).stream())
														.toList())
						.extractedRecipeInstructions(instructions)
						.calories(source.getNutrition().getCalories())
						.fatContent(source.getNutrition().getFatContent())
						.carbohydrateContent(source.getNutrition().getCarbohydrateContent())
						.sugarContent(source.getNutrition().getSugarContent())
						.fiberContent(source.getNutrition().getFiberContent())
						.proteinContent(source.getNutrition().getProteinContent())
						.sodiumContent(source.getNutrition().getSodiumContent())
						.imageUrl(getImageUrl(source.getImage()))
						.extractionMethod("JSON-LD")
						.build();
	}

	private List<ExtractedRecipeInstruction> extractInstructions(final JsonNode recipeInstructions) {
		final List<ExtractedRecipeInstruction> instructions;

		if (!recipeInstructions.isEmpty()) {
			instructions = new ArrayList<>();
			// extract as an array of instructions
			for (final JsonNode recipeInstruction : recipeInstructions) {
				JsonLdHowToStep step;
				try {
					step = objectMapper.treeToValue(recipeInstruction, JsonLdHowToStep.class);
				} catch (final JsonProcessingException e) {
					step = JsonLdHowToStep.builder().text(recipeInstruction.asText()).build();
				}
				instructions.add(convertInstruction(step));
			}
		} else {
			// extract as a single instruction
			instructions = Arrays.stream(recipeInstructions.asText().split("\n"))
							.map(in -> ExtractedRecipeInstruction.builder().text(in).build()).toList();
		}

		return instructions;
	}

	private ExtractedRecipeInstruction convertInstruction(final JsonLdHowToStep instruction) {
		return ExtractedRecipeInstruction.builder()
						.text(instruction.getText())
						.type(instruction.getType())
						.build();
	}

	private String getImageUrl(final JsonNode image) {
		if (image.get("@type") != null && image.get("@type").asText().equals("ImageObject")) {
			return image.get("url").asText();
		} else if (image instanceof ArrayNode) {
			return getImageUrl(image.get(0));
		} else {
			return image.asText();
		}
	}
}
