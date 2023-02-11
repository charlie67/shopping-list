package to.charlie.foodplanner.domain.dao;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import to.charlie.foodplanner.domain.entity.RecipeEntity;


public interface RecipeRepository extends CrudRepository<RecipeEntity, UUID>
{
}
