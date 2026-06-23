package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends CrudRepository<IngredientEntity, UUID> {

	Optional<IngredientEntity> findByName(String name);
}
