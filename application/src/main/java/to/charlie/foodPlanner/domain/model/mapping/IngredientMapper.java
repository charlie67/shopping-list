package to.charlie.foodPlanner.domain.model.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;

@Component
@RequiredArgsConstructor
public class IngredientMapper {

  public IngredientEntity dtoToEntity(final IngredientDto ingredientDto) {
    return IngredientEntity.builder()
        .name(ingredientDto.getIngredientName())
        .build();
  }

  public IngredientDto entityToDto(final IngredientEntity ingredientEntity) {
    return IngredientDto.builder()
        .ingredientName(ingredientEntity.getName())
        .build();
  }

  public IngredientDto entityToDto(final RecipeIngredientEntity recipeIngredientEntity) {
    return IngredientDto.builder()
        .ingredientName(recipeIngredientEntity.getIngredient().getName())
        .quantity(recipeIngredientEntity.getQuantity())
        .unit(recipeIngredientEntity.getUnit())
        .id(recipeIngredientEntity.getId())
        .build();
  }
}
