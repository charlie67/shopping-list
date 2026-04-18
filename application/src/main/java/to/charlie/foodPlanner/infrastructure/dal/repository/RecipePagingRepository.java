package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

public interface RecipePagingRepository extends PagingAndSortingRepository<RecipeEntity, UUID> {

  Page<RecipeEntity> findAll(Pageable pageable);

  Set<RecipeEntity> findAll();
}
