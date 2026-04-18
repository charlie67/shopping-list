package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

public interface RecipeRepository extends CrudRepository<RecipeEntity, UUID> {

  boolean existsByUrl(String url);

  Optional<RecipeEntity> findByUrl(String url);
}
