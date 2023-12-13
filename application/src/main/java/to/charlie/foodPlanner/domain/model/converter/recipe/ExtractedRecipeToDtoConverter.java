package to.charlie.foodPlanner.domain.model.converter.recipe;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
public class ExtractedRecipeToDtoConverter implements Converter<ExtractedRecipe, ExtractedRecipeDto> {

  @Override
  public ExtractedRecipeDto convert(ExtractedRecipe source) {
    return null;
  }
}
