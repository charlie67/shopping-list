package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdHowToStep;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdNutritionInformation;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonLdRecipeBuilder {

	private final IngredientBreakdownService ingredientExtractor;
	private final ObjectMapper objectMapper;

	public ExtractedRecipe convert(final JsonLdRecipe source) {
		final List<ExtractedRecipeInstruction> instructions = extractInstructions(
						source.getRecipeInstructions());
		// plenty of recipes carry no nutrition block at all
		final JsonLdNutritionInformation nutrition = source.getNutrition() != null
						? source.getNutrition()
						: new JsonLdNutritionInformation();

		return ExtractedRecipe.builder().url(source.getUrl())
						.name(source.getName())
						.description(source.getDescription())
						.dateModified(source.getDateModified())
						.datePublished(source.getDatePublished())
						.keywords(extractKeywords(source.getKeywords()))
						.cookTime(source.getCookTime())
						.prepTime(source.getPrepTime())
						.totalTime(source.getTotalTime())
						.recipeCategory(firstOrEmpty(source.getRecipeCategory()))// todo
						.recipeYield(firstOrEmpty(
										source.getRecipeYield()))// todo map these as lists all the way down
						.extractedRecipeIngredients(
										orEmpty(source.getRecipeIngredients()).stream()
														.flatMap(ingredient -> ingredientExtractor.convertIngredient(ingredient).stream())
														.toList())
						.extractedRecipeInstructions(instructions)
						.calories(nutrition.getCalories())
						.fatContent(nutrition.getFatContent())
						.saturatedFatContent(nutrition.getSaturatedFatContent())
						.carbohydrateContent(nutrition.getCarbohydrateContent())
						.sugarContent(nutrition.getSugarContent())
						.fiberContent(nutrition.getFiberContent())
						.proteinContent(nutrition.getProteinContent())
						.sodiumContent(nutrition.getSodiumContent())
						.imageUrl(getImageUrl(source.getImage()))
						.extractionMethod(ExtractionMethod.JSON_LD)
						.build();
	}

	private List<String> orEmpty(final List<String> values) {
		return values != null ? values : List.of();
	}

	private String firstOrEmpty(final List<String> values) {
		return orEmpty(values).stream().findFirst().orElse("");
	}

	/**
	 * Keywords come either as a list or as a single comma separated string.
	 */
	private List<String> extractKeywords(final JsonNode keywords) {
		if (keywords == null || keywords.isNull()) {
			return List.of();
		}

		if (keywords.isArray()) {
			final List<String> extracted = new ArrayList<>();
			for (final JsonNode keyword : keywords) {
				extracted.add(keyword.asText());
			}
			return extracted;
		}

		return Arrays.stream(keywords.asText().split(","))
						.map(String::trim)
						.filter(keyword -> !keyword.isEmpty())
						.toList();
	}

	private List<ExtractedRecipeInstruction> extractInstructions(final JsonNode recipeInstructions) {
		final List<ExtractedRecipeInstruction> instructions;

		if (recipeInstructions == null || recipeInstructions.isNull()) {
			return List.of();
		}

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
		if (image == null || image.isMissingNode() || image.isNull()) {
			return null;
		}

		if (image.get("@type") != null && image.get("@type").asText().equals("ImageObject")) {
			return image.get("url").asText();
		} else if (image instanceof ArrayNode) {
			return getImageUrl(image.get(0));
		} else {
			return image.asText();
		}
	}
}
