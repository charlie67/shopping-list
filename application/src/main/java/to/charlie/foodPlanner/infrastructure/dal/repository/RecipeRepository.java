package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends CrudRepository<RecipeEntity, UUID> {

	boolean existsByUrl(String url);

	Optional<RecipeEntity> findByUrl(String url);
}
