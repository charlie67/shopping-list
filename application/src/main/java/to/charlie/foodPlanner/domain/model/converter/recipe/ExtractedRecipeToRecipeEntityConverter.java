package to.charlie.foodPlanner.domain.model.converter.recipe;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeStepEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.infrastructure.dal.dao.IngredientDao;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExtractedRecipeToRecipeEntityConverter implements
				Converter<ExtractedRecipe, RecipeEntity> {

	final IngredientDao ingredientDao;
	final ModelMapper modelMapper;

	@Override
	public RecipeEntity convert(final ExtractedRecipe recipe) {
		final AtomicInteger instructionCount = new AtomicInteger(1);

		final RecipeEntity recipeEntity = RecipeEntity.builder()
						.name(recipe.getName())
						.url(recipe.getUrl())
						.description(recipe.getDescription())
						.dateModified(recipe.getDateModified())
						.datePublished(recipe.getDatePublished())
						.keywords(recipe.getKeywords() == null ? "" : recipe.getKeywords().getFirst())
						.cookTime(recipe.getCookTime())
						.prepTime(recipe.getPrepTime())
						.totalTime(recipe.getTotalTime())
						.recipeCategory(recipe.getRecipeCategory())
						.recipeYield(recipe.getRecipeYield())
						.calories(recipe.getCalories())
						.fatContent(recipe.getFatContent())
						.saturatedFatContent(recipe.getSaturatedFatContent())
						.carbohydrateContent(recipe.getCarbohydrateContent())
						.sugarContent(recipe.getSugarContent())
						.fiberContent(recipe.getFiberContent())
						.proteinContent(recipe.getProteinContent())
						.sodiumContent(recipe.getSodiumContent())
						.extractionMethod(recipe.getExtractionMethod())
						.imageUrl(recipe.getImageUrl())
						.ingredients(recipe.getExtractedRecipeIngredients()
										.stream()
										.map(extractedIngredient -> RecipeIngredientEntity.builder()
														.ingredient(
																		ingredientDao.findOrCreateIngredient(extractedIngredient.getIngredient().getName()))
														.quantity(extractedIngredient.getQuantity() == null ? 0 : extractedIngredient.getQuantity())
														.unit(extractedIngredient.getUnit())
														.quantityUnit(extractedIngredient.getQuantityUnit())
														.quantityText(extractedIngredient.getQuantityText())
														.wholeText(extractedIngredient.getFullText())
														.preparation(extractedIngredient.getPreparation())
														.size(extractedIngredient.getSize())
														.comment(extractedIngredient.getComment())
														.purpose(extractedIngredient.getPurpose())
														.build())
										.collect(Collectors.toSet()))
						.steps(recipe.getExtractedRecipeInstructions()
										.stream()
										.map(instruction -> RecipeStepEntity.builder()
														.text(instruction.getText())
														.type(instruction.getType())
														.stepCount(instructionCount.getAndIncrement())
														.build())
										.collect(Collectors.toSet()))
						.build();

		recipeEntity.getIngredients()
						.forEach(ingredient -> ingredient.setRecipe(recipeEntity));
		recipeEntity.getSteps()
						.forEach(step -> step.setRecipe(recipeEntity));

		return recipeEntity;
	}
}
