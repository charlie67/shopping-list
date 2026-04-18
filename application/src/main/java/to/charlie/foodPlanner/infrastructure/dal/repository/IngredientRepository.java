package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;

public interface IngredientRepository extends CrudRepository<IngredientEntity, UUID> {

  Optional<IngredientEntity> findByName(String name);
}
