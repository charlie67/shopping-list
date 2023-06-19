package to.charlie.foodPlanner.domain.model.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.dao.IngredientDao;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;

@RequiredArgsConstructor
@Component
public class RecipeIngredientMapper {

  private final IngredientDao ingredientDao;

  public RecipeIngredientEntity dtoToEntity(final IngredientDto ingredientDto) {
    return RecipeIngredientEntity.builder().quantity(ingredientDto.getQuantity())
        .unit(ingredientDto.getUnit())
        .ingredient(ingredientDao.findOrCreateIngredient(ingredientDto.getIngredientName()))
        .build();
  }
}
