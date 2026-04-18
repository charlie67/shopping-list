package to.charlie.foodPlanner.infrastructure.dal.dao;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.infrastructure.dal.repository.IngredientRepository;

@Component
@RequiredArgsConstructor
public class IngredientDao {

  private final IngredientRepository ingredientRepository;

  public IngredientEntity findOrCreateIngredient(final String ingredient) {
    final Optional<IngredientEntity> ingredientEntityOptional = ingredientRepository.findByName(
        ingredient);
    if (ingredientEntityOptional.isEmpty()) {
      final IngredientEntity ingredientEntity = IngredientEntity.builder().name(ingredient).build();
      ingredientRepository.save(ingredientEntity);

      return ingredientEntity;
    } else {
      return ingredientEntityOptional.get();
    }
  }
}
