package to.charlie.foodPlanner.domain.dal.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.repository.RecipeIngredientRepository;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;

@Component
@RequiredArgsConstructor
public class RecipeIngredientDao {
	
private final RecipeIngredientRepository recipeIngredientRepository;

	public RecipeIngredientEntity save(final RecipeIngredientEntity recipeIngredientEntity) {
		return recipeIngredientRepository.save(recipeIngredientEntity);
	}
}
