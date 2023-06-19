package to.charlie.foodPlanner.domain.dal.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.repository.RecipeRepository;
import to.charlie.foodPlanner.domain.model.entity.RecipeEntity;

@Component
@RequiredArgsConstructor
public class RecipeDao {

  private final RecipeRepository recipeRepository;

  public RecipeEntity save(final RecipeEntity recipeEntity) {
    return recipeRepository.save(recipeEntity);
  }
}
