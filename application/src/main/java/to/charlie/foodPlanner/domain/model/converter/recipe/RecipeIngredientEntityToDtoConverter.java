package to.charlie.foodPlanner.domain.model.converter.recipe;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;

@Component
public class RecipeIngredientEntityToDtoConverter implements Converter<RecipeIngredientEntity, IngredientDto> {
  @Override
  public IngredientDto convert(RecipeIngredientEntity source) {
    return IngredientDto.builder()
            .ingredientName(source.getIngredient().getName())
            .quantity(source.getQuantity())
            .unit(source.getUnit())
            .id(source.getId())
            .build();
  }
}
