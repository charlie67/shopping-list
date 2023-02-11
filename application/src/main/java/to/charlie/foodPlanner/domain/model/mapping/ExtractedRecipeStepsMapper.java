package to.charlie.foodPlanner.domain.model.mapping;

import java.util.List;
import java.util.stream.Collectors;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeStepsDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeStep;

public class ExtractedRecipeStepsMapper {

  private static ExtractedRecipeStepsDto map(ExtractedRecipeStep extractedRecipe) {
    return ExtractedRecipeStepsDto.builder()
        .step(extractedRecipe.getOriginalText())
        .possibleDuplicate(extractedRecipe.isPossibleDuplicate())
        .build();
  }

  public static List<ExtractedRecipeStepsDto> map(List<ExtractedRecipeStep> extractedRecipeSteps) {
    return extractedRecipeSteps.stream()
        .map(ExtractedRecipeStepsMapper::map)
        .collect(Collectors.toList());
  }
}
