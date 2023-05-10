package to.charlie.foodPlanner.domain.dal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

@Repository
public interface TodoRepository extends JpaRepository<ShoppingListItemEntity, Long> {

  long countByCompletedTrue();

  List<ShoppingListItemEntity> findAllByCompleted(boolean completed);

  Optional<ShoppingListItemEntity> findById(long Id);

  Long countByCompleted(boolean completed);
}
