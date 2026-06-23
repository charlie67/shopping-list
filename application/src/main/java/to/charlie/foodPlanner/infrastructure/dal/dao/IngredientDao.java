package to.charlie.foodPlanner.infrastructure.dal.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.infrastructure.dal.repository.IngredientRepository;

@Component
@RequiredArgsConstructor
public class IngredientDao {

  private final IngredientRepository ingredientRepository;

  public IngredientEntity findOrCreateIngredient(final String ingredient) {
    return ingredientRepository.findByName(ingredient)
        .orElseGet(() -> {
          try {
            return ingredientRepository.save(
                IngredientEntity.builder().name(ingredient).build());
          } catch (final DataIntegrityViolationException e) {
            return ingredientRepository.findByName(ingredient)
                .orElseThrow(() -> e);
          }
        });
  }
}
