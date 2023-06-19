package to.charlie.foodPlanner.domain.model.mapping;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.dto.RecipeDto;
import to.charlie.foodPlanner.domain.model.entity.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

  private final RecipeIngredientMapper recipeIngredientMapper;

  private final IngredientMapper ingredientMapper;

  public RecipeDto entityToDto(final RecipeEntity recipeEntity) {
    return RecipeDto.builder()
        .id(recipeEntity.getId())
        .steps(recipeEntity.getInstructions())
        .url(recipeEntity.getUrl())
        .title(recipeEntity.getTitle())
        .ingredients(recipeEntity.getIngredients().stream().map(ingredientMapper::entityToDto)
            .collect(Collectors.toSet()))
        .build();
  }

  public RecipeEntity dtoToEntity(final RecipeDto recipeDto) {
    return RecipeEntity.builder()
        .id(recipeDto.getId())
        .instructions(recipeDto.getSteps())
        .ingredients(getAllIngredientsForRecipe(recipeDto))
        .url(recipeDto.getUrl())
        .title(recipeDto.getTitle())
        .build();
  }

  private Set<RecipeIngredientEntity> getAllIngredientsForRecipe(final RecipeDto recipeDto) {
    final Set<RecipeIngredientEntity> ingredients = new LinkedHashSet<>();
    for (final IngredientDto ingredientDto : recipeDto.getIngredients()) {
      ingredients.add(recipeIngredientMapper.dtoToEntity(ingredientDto));
    }
    return ingredients;
  }
}
