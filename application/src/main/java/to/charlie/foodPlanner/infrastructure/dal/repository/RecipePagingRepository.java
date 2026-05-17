package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

import java.util.Set;
import java.util.UUID;

@Repository
public interface RecipePagingRepository extends PagingAndSortingRepository<RecipeEntity, UUID> {

	Page<RecipeEntity> findAll(Pageable pageable);

	Set<RecipeEntity> findAll();
}
