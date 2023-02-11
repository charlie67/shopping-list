package to.charlie.foodplanner.domain.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import to.charlie.foodplanner.domain.entity.IngredientEntity;


public interface Ingredientrepository extends CrudRepository<IngredientEntity, UUID>
{
  Optional<IngredientEntity> findByIngredient(String ingredient);
}
