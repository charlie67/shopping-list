package to.charlie.foodPlanner.infrastructure.dal.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeIngredientRepository;

@Component
@RequiredArgsConstructor
public class RecipeIngredientDao {

  private final RecipeIngredientRepository recipeIngredientRepository;

  public RecipeIngredientEntity save(final RecipeIngredientEntity recipeIngredientEntity) {
    return recipeIngredientRepository.save(recipeIngredientEntity);
  }
}
