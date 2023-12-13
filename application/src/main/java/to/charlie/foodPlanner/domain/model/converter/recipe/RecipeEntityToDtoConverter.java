package to.charlie.foodPlanner.domain.model.converter.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.dto.recipe.RecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeEntityToDtoConverter implements Converter<RecipeEntity, RecipeDto> {

  private final Converter<RecipeIngredientEntity, IngredientDto> converter;

  @Override
  public RecipeDto convert(RecipeEntity source) {
    return RecipeDto.builder()
            .id(source.getId())
            .steps("source.getSteps()")
            .url(source.getUrl())
            .name(source.getName())
            .ingredients(source.getIngredients().stream().map(converter::convert)
                    .collect(Collectors.toSet()))
            .build();
  }
}
