package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

@Repository
public interface TodoPagingRepository
    extends PagingAndSortingRepository<ShoppingListItemEntity, UUID> {

  Page<ShoppingListItemEntity> findAllByCompleted(boolean completed, Pageable pageable);
}
