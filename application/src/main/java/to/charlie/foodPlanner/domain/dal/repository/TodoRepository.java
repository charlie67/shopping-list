package to.charlie.foodPlanner.domain.dal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

@Repository
public interface TodoRepository extends JpaRepository<ShoppingListItemEntity, UUID> {

  long countByCompletedTrue();

  List<ShoppingListItemEntity> findAllByCompleted(boolean completed);

  Long countByCompleted(boolean completed);
}
