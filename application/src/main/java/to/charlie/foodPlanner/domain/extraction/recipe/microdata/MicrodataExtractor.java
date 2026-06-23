package to.charlie.foodPlanner.domain.extraction.recipe.microdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MicrodataExtractor implements RecipeExtractor {

	private final IngredientBreakdownService ingredientBreakdownService;

	public ExtractedRecipe extract(final Document document, final String url)
					throws RecipeExtractionFailed {

		// find all tags with the itemprop attribute
		final Elements itemPropElements = document.select("[itemprop]");

		String name = null;
		String description = null;
		String dateModified = null;
		String datePublished = null;
		final List<String> keywords = new ArrayList<>();
		String cookTime = null;
		String prepTime = null;
		String totalTime = null;
		String recipeCategory = null;
		String recipeYield = null;
		final List<ExtractedRecipeIngredient> extractedRecipeIngredients = new ArrayList<>();
		final List<ExtractedRecipeInstruction> extractedRecipeInstructions = new ArrayList<>();
		String calories = null;
		String fatContent = null;
		String saturatedFatContent = null;
		String carbohydrateContent = null;
		String sugarContent = null;
		String fiberContent = null;
		String proteinContent = null;
		String sodiumContent = null;

		for (final Element itemPropElement : itemPropElements) {
			final String type = itemPropElement.attr("itemprop");

			if (type.equals("name")) {
				name = itemPropElement.text();
			} else if (type.equals("description")) {
				description = itemPropElement.text();
			} else if (type.equals("dateModified")) {
				dateModified = itemPropElement.text();
			} else if (type.equals("datePublished")) {
				datePublished = itemPropElement.text();
			} else if (type.equals("keywords")) {
				keywords.add(itemPropElement.text());
			} else if (type.equals("cookTime")) {
				cookTime = itemPropElement.text();
			} else if (type.equals("prepTime")) {
				prepTime = itemPropElement.text();
			} else if (type.equals("totalTime")) {
				totalTime = itemPropElement.text();
			} else if (type.equals("recipeCategory")) {
				recipeCategory = itemPropElement.text();
			} else if (type.equals("recipeYield")) {
				recipeYield = itemPropElement.text();
			} else if (type.equals("recipeIngredient")) {
				extractedRecipeIngredients.addAll(
								ingredientBreakdownService.convertIngredient(itemPropElement.text()));
			} else if (type.equals("recipeInstructions")) {
				extractedRecipeInstructions.add(ExtractedRecipeInstruction.builder()
								.text(itemPropElement.text()).build());
			} else if (type.equals("calories")) {
				calories = itemPropElement.text();
			} else if (type.equals("fatContent")) {
				fatContent = itemPropElement.text();
			} else if (type.equals("saturatedFatContent")) {
				saturatedFatContent = itemPropElement.text();
			} else if (type.equals("carbohydrateContent")) {
				carbohydrateContent = itemPropElement.text();
			} else if (type.equals("sugarContent")) {
				sugarContent = itemPropElement.text();
			} else if (type.equals("fiberContent")) {
				fiberContent = itemPropElement.text();
			} else if (type.equals("proteinContent")) {
				proteinContent = itemPropElement.text();
			} else if (type.equals("sodiumContent")) {
				sodiumContent = itemPropElement.text();
			}
		}

		if (name == null || extractedRecipeIngredients.isEmpty()
						|| extractedRecipeInstructions.isEmpty()) {
			throw new RecipeExtractionFailed(String.format(
							"Unable to extract a recipe using Microdata, name: %s, ingredientSize: %s, recipeInstructionSize: %s",
							name, extractedRecipeIngredients.size(), extractedRecipeInstructions.size()));
		}

		return ExtractedRecipe.builder()
						.name(name)
						.description(description)
						.dateModified(dateModified)
						.datePublished(datePublished)
						.keywords(keywords)
						.cookTime(cookTime)
						.prepTime(prepTime)
						.totalTime(totalTime)
						.recipeCategory(recipeCategory)
						.recipeYield(recipeYield)
						.extractedRecipeIngredients(extractedRecipeIngredients)
						.extractedRecipeInstructions(extractedRecipeInstructions)
						.calories(calories)
						.fatContent(fatContent)
						.saturatedFatContent(saturatedFatContent)
						.carbohydrateContent(carbohydrateContent)
						.sugarContent(sugarContent)
						.fiberContent(fiberContent)
						.proteinContent(proteinContent)
						.sodiumContent(sodiumContent)
						.extractionMethod("microdata")
						.build();
	}
}
