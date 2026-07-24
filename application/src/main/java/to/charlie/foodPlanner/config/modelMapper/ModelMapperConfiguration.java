package to.charlie.foodPlanner.config.modelMapper;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedIngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeStepsDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeStepEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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

		// The DTO exposes the extraction method as its human-readable display name (e.g. "JSON-LD"),
		// while the internal model and entity hold the enum, so convert via the enum's name field.
		final Converter<ExtractionMethod, String> extractionMethodToName =
						ctx -> ctx.getSource() == null ? null : ctx.getSource().getName();
		final Converter<String, ExtractionMethod> extractionMethodFromName =
						ctx -> ctx.getSource() == null || ctx.getSource().isBlank()
										? null
										: ExtractionMethod.fromName(ctx.getSource());

		// Keywords are a list internally but a single column on the entity and a single field on the DTO,
		// so they are flattened to a comma-separated string and split back out on the way in.
		final Converter<List<String>, String> joinKeywords =
						ctx -> ctx.getSource() == null ? "" : String.join(", ", ctx.getSource());

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

		// Map ingredients into an order-preserving LinkedHashSet and bake the recipe order into
		// ingredientOrder, so the collection keeps its original order when read back from the database
		// instead of relying on the iteration order of a HashSet. This mirrors how steps use stepCount.
		final Converter<List<ExtractedRecipeIngredient>, Set<RecipeIngredientEntity>> orderedIngredients = ctx -> {
			final Set<RecipeIngredientEntity> ingredients = new LinkedHashSet<>();
			if (ctx.getSource() == null) {
				return ingredients;
			}
			final AtomicInteger ingredientOrder = new AtomicInteger(0);
			ctx.getSource().forEach(ingredient -> {
				final RecipeIngredientEntity entity = modelMapper.map(ingredient, RecipeIngredientEntity.class);
				entity.setIngredientOrder(ingredientOrder.getAndIncrement());
				ingredients.add(entity);
			});
			return ingredients;
		};

		modelMapper.typeMap(ExtractedRecipe.class, RecipeEntity.class)
						.addMappings(mapper -> {
							mapper.using(joinKeywords).map(ExtractedRecipe::getKeywords, RecipeEntity::setKeywords);
							mapper.using(orderedIngredients).map(ExtractedRecipe::getExtractedRecipeIngredients, RecipeEntity::setIngredients);
							mapper.using(orderedSteps).map(ExtractedRecipe::getExtractedRecipeInstructions, RecipeEntity::setSteps);
						});

		// The DTO carries a flat ingredient name and differently named collections, and the internal model
		// types are @Data + @Builder with no no-arg constructor, so ModelMapper cannot instantiate them
		// itself. Building them here through their builders keeps the whole DTO -> internal leg explicit.
		final Converter<List<ExtractedIngredientDto>, List<ExtractedRecipeIngredient>> toRecipeIngredients = ctx -> {
			if (ctx.getSource() == null) {
				return List.of();
			}
			return ctx.getSource().stream()
							.map(dto -> ExtractedRecipeIngredient.builder()
											.fullText(dto.getFullText())
											.ingredient(ExtractedIngredient.builder().name(dto.getIngredientName()).build())
											.quantity(dto.getQuantity())
											.quantityText(dto.getQuantityText())
											.unit(dto.getUnit())
											.size(dto.getSize())
											.preparation(dto.getPreparation())
											.comment(dto.getComment())
											.purpose(dto.getPurpose())
											.build())
							.toList();
		};

		// Instruction order is significant: the ExtractedRecipe -> RecipeEntity mapping derives stepCount
		// from list position, so the incoming order is preserved as-is.
		final Converter<List<ExtractedRecipeStepsDto>, List<ExtractedRecipeInstruction>> toRecipeInstructions = ctx -> {
			if (ctx.getSource() == null) {
				return List.of();
			}
			return ctx.getSource().stream()
							.map(dto -> ExtractedRecipeInstruction.builder()
											.text(dto.getText())
											.type(dto.getType())
											.build())
							.toList();
		};

		final Converter<String, List<String>> splitKeywords =
						ctx -> ctx.getSource() == null || ctx.getSource().isBlank()
										? List.of()
										: Arrays.stream(ctx.getSource().split(",")).map(String::trim).filter(k -> !k.isEmpty()).toList();

		// Without this the DTO's flat keyword string would be filled from List::toString, brackets and all.
		modelMapper.typeMap(ExtractedRecipe.class, ExtractedRecipeDto.class)
						.addMappings(mapper -> {
							mapper.using(joinKeywords).map(ExtractedRecipe::getKeywords, ExtractedRecipeDto::setKeywords);
							mapper.using(extractionMethodToName)
											.map(ExtractedRecipe::getExtractionMethod, ExtractedRecipeDto::setExtractionMethod);
						});

		modelMapper.typeMap(ExtractedRecipeDto.class, ExtractedRecipe.class)
						.addMappings(mapper -> {
							mapper.using(toRecipeIngredients)
											.map(ExtractedRecipeDto::getIngredients, ExtractedRecipe::setExtractedRecipeIngredients);
							mapper.using(toRecipeInstructions)
											.map(ExtractedRecipeDto::getInstructions, ExtractedRecipe::setExtractedRecipeInstructions);
							mapper.using(splitKeywords).map(ExtractedRecipeDto::getKeywords, ExtractedRecipe::setKeywords);
							mapper.using(extractionMethodFromName)
											.map(ExtractedRecipeDto::getExtractionMethod, ExtractedRecipe::setExtractionMethod);
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
						.addMappings(mapper -> {
							mapper.map(RecipeEntity::getSteps, ExtractedRecipeDto::setInstructions);
							mapper.using(extractionMethodToName)
											.map(RecipeEntity::getExtractionMethod, ExtractedRecipeDto::setExtractionMethod);
						});

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
