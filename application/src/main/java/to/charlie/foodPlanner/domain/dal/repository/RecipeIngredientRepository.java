package to.charlie.foodPlanner.domain.dal.repository;

import org.springframework.data.repository.CrudRepository;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;

import java.util.UUID;

public interface RecipeIngredientRepository  extends CrudRepository<RecipeIngredientEntity, UUID> {
}
