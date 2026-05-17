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
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class ModelMapperConfiguration {

	@Bean
	public ModelMapper modelMapper() {
		final ModelMapper modelMapper = new ModelMapper();

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
