package to.charlie.foodPlanner.domain.model.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.domain.repository.IngredientRepository;

@Component
@RequiredArgsConstructor
public class IngredientDao {

  private final IngredientRepository ingredientRepository;

  public void saveIngredientIfDoesntExist(IngredientEntity ingredientEntity) {
    if (ingredientRepository.findByName(ingredientEntity.getName()).isEmpty()) {
      ingredientRepository.save(ingredientEntity);
    }
  }
}
