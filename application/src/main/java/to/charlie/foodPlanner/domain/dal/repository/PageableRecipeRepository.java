package to.charlie.foodPlanner.domain.dal.repository;

import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

public interface PageableRecipeRepository extends PagingAndSortingRepository<RecipeEntity, UUID> {

}
