package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;

import java.util.UUID;

@Repository
public interface RecipeIngredientRepository extends CrudRepository<RecipeIngredientEntity, UUID> {

}
