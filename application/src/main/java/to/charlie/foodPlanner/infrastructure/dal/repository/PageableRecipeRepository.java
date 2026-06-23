package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

import java.util.UUID;

@Repository
public interface PageableRecipeRepository extends PagingAndSortingRepository<RecipeEntity, UUID> {

}
