package to.charlie.foodPlanner.domain.dal.repository;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;

public interface RecipeIngredientRepository extends CrudRepository<RecipeIngredientEntity, UUID> {

}
