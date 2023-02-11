package to.charlie.foodPlanner.domain.model.mapping;

import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.RecipeDto;
import to.charlie.foodPlanner.domain.model.entity.RecipeEntity;

@Component
public class RecipeMapper {

  public RecipeDto entityToDto(RecipeEntity recipeEntity) {
    return RecipeDto.builder()
        .id(recipeEntity.getId())
        .steps(recipeEntity.getInstructions())
        .url(recipeEntity.getUrl())
        .title(recipeEntity.getTitle())
        .build();
  }
}
