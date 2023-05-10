package to.charlie.foodPlanner.domain.dal.repository;

import org.springframework.data.repository.CrudRepository;
import to.charlie.foodPlanner.domain.model.entity.RecipeEntity;

import java.util.UUID;

public interface RecipeRepository extends CrudRepository<RecipeEntity, UUID> {
}
