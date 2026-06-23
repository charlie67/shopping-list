package to.charlie.foodPlanner.config.modelMapper;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedIngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeStepEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ModelMapperConfiguration {

	@Bean
	public ModelMapper modelMapper() {
		final ModelMapper modelMapper = new ModelMapper();

		// Null source values must not overwrite primitive targets (e.g. nullable Double quantity -> primitive double).
		modelMapper.getConfiguration().setSkipNullEnabled(true);

		modelMapper.addMappings(new PropertyMap<ExtractedRecipeIngredient, RecipeIngredientEntity>() {
			@Override
			protected void configure() {
				map().setWholeText(source.getFullText());
			}
		});

		final Converter<List<String>, String> firstKeywordOrEmpty =
						ctx -> ctx.getSource() == null || ctx.getSource().isEmpty() ? "" : ctx.getSource().getFirst();

		// Map instructions in their original order, baking the recipe order into stepCount. This keeps the
		// step ordering deterministic instead of relying on the iteration order of a HashSet.
		final Converter<List<ExtractedRecipeInstruction>, Set<RecipeStepEntity>> orderedSteps = ctx -> {
			final Set<RecipeStepEntity> steps = new LinkedHashSet<>();
			if (ctx.getSource() == null) {
				return steps;
			}
			final AtomicInteger stepCount = new AtomicInteger(1);
			ctx.getSource().forEach(instruction -> steps.add(RecipeStepEntity.builder()
							.text(instruction.getText())
							.type(instruction.getType())
							.stepCount(stepCount.getAndIncrement())
							.build()));
			return steps;
		};

		// Map ingredients into an order-preserving LinkedHashSet so the result keeps the original ingredient
		// order instead of relying on the iteration order of a HashSet.
		final Converter<List<ExtractedRecipeIngredient>, Set<RecipeIngredientEntity>> orderedIngredients = ctx -> {
			final Set<RecipeIngredientEntity> ingredients = new LinkedHashSet<>();
			if (ctx.getSource() == null) {
				return ingredients;
			}
			ctx.getSource().forEach(ingredient ->
							ingredients.add(modelMapper.map(ingredient, RecipeIngredientEntity.class)));
			return ingredients;
		};

		modelMapper.typeMap(ExtractedRecipe.class, RecipeEntity.class)
						.addMappings(mapper -> {
							mapper.using(firstKeywordOrEmpty).map(ExtractedRecipe::getKeywords, RecipeEntity::setKeywords);
							mapper.using(orderedIngredients).map(ExtractedRecipe::getExtractedRecipeIngredients, RecipeEntity::setIngredients);
							mapper.using(orderedSteps).map(ExtractedRecipe::getExtractedRecipeInstructions, RecipeEntity::setSteps);
						});

		modelMapper.addMappings(new PropertyMap<ExtractedRecipeIngredient, ExtractedIngredientDto>() {
			@Override
			protected void configure() {
				map().setIngredientName(source.getIngredient().getName());
				map().setUnit(source.getUnit());
				map().setFullText(source.getFullText());
			}
		});

		modelMapper.addMappings(new PropertyMap<RecipeIngredientEntity, ExtractedIngredientDto>() {
			@Override
			protected void configure() {
				map().setIngredientName(source.getIngredient().getName());
				map().setFullText(source.getWholeText());
			}
		});

		modelMapper.addMappings(new PropertyMap<RecipeIngredientEntity, IngredientDto>() {
			@Override
			protected void configure() {
				map().setIngredientName(source.getIngredient().getName());
			}
		});

		// use typeMap because these are just generic collections.
		modelMapper.typeMap(RecipeEntity.class, ExtractedRecipeDto.class)
						.addMapping(RecipeEntity::getSteps, ExtractedRecipeDto::setInstructions);

		final Converter<LocalDateTime, Long> toEpochSeconds =
						ctx -> ctx.getSource().toEpochSecond(ZoneOffset.UTC);
		modelMapper.typeMap(ShoppingListItemEntity.class, ShoppingListItemDto.class)
						.addMappings(mapper -> {
							mapper.using(toEpochSeconds).map(ShoppingListItemEntity::getCreatedAtTime, ShoppingListItemDto::setCreatedAtTime);
							mapper.using(toEpochSeconds).map(ShoppingListItemEntity::getUpdatedAtTime, ShoppingListItemDto::setUpdatedAtTime);
						});

		return modelMapper;
	}
}
