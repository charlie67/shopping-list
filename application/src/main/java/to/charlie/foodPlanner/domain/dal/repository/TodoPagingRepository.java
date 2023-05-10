package to.charlie.foodPlanner.domain.dal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

@Repository
public interface TodoPagingRepository
    extends PagingAndSortingRepository<ShoppingListItemEntity, Long> {
  Page<ShoppingListItemEntity> findAllByCompleted(boolean completed, Pageable pageable);
}
