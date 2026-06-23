package to.charlie.foodPlanner.infrastructure.dal.dao;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.infrastructure.dal.repository.PageableRecipeRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecipeDao {

	private final RecipeRepository recipeRepository;
	private final PageableRecipeRepository pageableRecipeRepository;
	private final ModelMapper modelMapper;
	private final IngredientDao ingredientDao;

	public RecipeEntity save(final RecipeEntity recipeEntity) {
		return recipeRepository.save(recipeEntity);
	}

	public RecipeEntity save(final ExtractedRecipe recipeDto) {
		final RecipeEntity entity = modelMapper.map(recipeDto, RecipeEntity.class);

		entity.getIngredients().forEach(ri -> {
			ri.setIngredient(ingredientDao.findOrCreateIngredient(ri.getIngredient().getName()));
			ri.setRecipe(entity);
		});

		// stepCount is assigned during mapping to preserve the recipe's original order.
		entity.getSteps().forEach(step -> step.setRecipe(entity));

		return save(entity);
	}

	public boolean existsByUrl(final String url) {
		return recipeRepository.existsByUrl(url);
	}

	public Page<ExtractedRecipeDto> findPage(final int page) {
		final Pageable pageable = PageRequest.of(page, 20);

		final Page<RecipeEntity> allRecipesPageable = pageableRecipeRepository.findAll(pageable);

		return allRecipesPageable.map(it -> modelMapper.map(it, ExtractedRecipeDto.class));
	}

	public Optional<ExtractedRecipe> findByUrl(final String url) {
		return recipeRepository.findByUrl(url)
						.map(entity -> modelMapper.map(entity, ExtractedRecipe.class));
	}
}
