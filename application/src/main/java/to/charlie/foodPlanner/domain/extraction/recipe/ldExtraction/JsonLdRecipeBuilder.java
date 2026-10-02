package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonLdRecipeBuilder {

	private final IngredientBreakdownService ingredientExtractor;
	private final JsonLdInstructionReader instructionReader;

	/**
	 * @param idIndex every {@code @id} seen anywhere in the page, so a recipe that references its
	 *                image or nutrition block rather than inlining it still resolves.
	 */
	public ExtractedRecipe convert(final JsonLdRecipe source, final Map<String, JsonNode> idIndex) {
		final List<ExtractedRecipeInstruction> instructions =
						instructionReader.read(source.getRecipeInstructions(), idIndex);
		// plenty of recipes carry no nutrition block at all
		final JsonNode nutrition =
						JsonLdValues.resolve(JsonLdValues.unwrap(source.getNutrition()), idIndex);

		return ExtractedRecipe.builder().url(JsonLdValues.url(source.getUrl(), idIndex))
						.name(JsonLdValues.text(source.getName()))
						.description(JsonLdValues.text(source.getDescription()))
						.dateModified(JsonLdValues.text(source.getDateModified()))
						.datePublished(JsonLdValues.text(source.getDatePublished()))
						.keywords(extractKeywords(source.getKeywords()))
						.cookTime(JsonLdValues.duration(source.getCookTime()))
						.prepTime(JsonLdValues.duration(source.getPrepTime()))
						.totalTime(JsonLdValues.duration(source.getTotalTime()))
						.recipeCategory(JsonLdValues.first(source.getRecipeCategory()))// todo
						.recipeYield(JsonLdValues.first(
										source.getRecipeYield()))// todo map these as lists all the way down
						.extractedRecipeIngredients(
										JsonLdValues.texts(source.getRecipeIngredients()).stream()
														.flatMap(ingredient -> ingredientExtractor.convertIngredient(ingredient).stream())
														.toList())
						.extractedRecipeInstructions(instructions)
						.calories(nutrient(nutrition, "calories"))
						.fatContent(nutrient(nutrition, "fatContent"))
						.saturatedFatContent(nutrient(nutrition, "saturatedFatContent"))
						.carbohydrateContent(nutrient(nutrition, "carbohydrateContent"))
						.sugarContent(nutrient(nutrition, "sugarContent"))
						.fiberContent(nutrient(nutrition, "fiberContent"))
						.proteinContent(nutrient(nutrition, "proteinContent"))
						.sodiumContent(nutrient(nutrition, "sodiumContent"))
						// todo use nutrition servingSize
						.imageUrl(JsonLdValues.url(source.getImage(), idIndex))
						.extractionMethod(ExtractionMethod.JSON_LD)
						.build();
	}

	private String nutrient(final JsonNode nutrition, final String property) {
		return nutrition == null ? null : JsonLdValues.text(nutrition.get(property));
	}

	/**
	 * Keywords come either as a list or as a single comma separated string. Only the string form is
	 * split - an entry in a list is one keyword however many commas it holds.
	 */
	private List<String> extractKeywords(final JsonNode keywords) {
		if (JsonLdValues.isAbsent(keywords)) {
			return List.of();
		}

		if (keywords.isArray()) {
			return JsonLdValues.texts(keywords);
		}

		final String joined = JsonLdValues.text(keywords);
		if (joined == null) {
			return List.of();
		}

		return Arrays.stream(joined.split(","))
						.map(String::trim)
						.filter(keyword -> !keyword.isEmpty())
						.toList();
	}
}
