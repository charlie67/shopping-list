package to.charlie.foodPlanner.domain.dal.dao;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.repository.IngredientRepository;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;

@Component
@RequiredArgsConstructor
public class IngredientDao {

  private final IngredientRepository ingredientRepository;

  public Set<IngredientEntity> findOrCreateIngredients(final Set<String> ingredients) {
    final Set<IngredientEntity> returnIngredients = new LinkedHashSet<>();
    for (final String ingredient : ingredients) {
      returnIngredients.add(findOrCreateIngredient(ingredient));
    }
    return returnIngredients;
  }

  public IngredientEntity findOrCreateIngredient(final String ingredient) {
    final Optional<IngredientEntity> ingredientEntityOptional = ingredientRepository.findByName(
        ingredient);
    if (ingredientEntityOptional.isEmpty()) {
      final IngredientEntity ingredientEntity = IngredientEntity.builder().name(ingredient).build();
      ingredientEntity.setName(ingredient);
      ingredientRepository.save(ingredientEntity);

      return ingredientEntity;
    } else {
      return ingredientEntityOptional.get();
    }
  }
}
