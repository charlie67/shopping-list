package to.charlie.foodPlanner.domain.model.mapping;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedIngredientDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;

@Component
public class ExtractedIngredientMapper {

  private static ExtractedIngredientDto map(ExtractedIngredient extractedIngredient) {
    return ExtractedIngredientDto.builder()
        .ingredientName(extractedIngredient.getIngredientName())
        .quantity(extractedIngredient.getQuantity().quantity())
        .unit(extractedIngredient.getQuantity().unit().name())
        .wholeName(extractedIngredient.getOriginalText())
        .possibleDuplicate(extractedIngredient.isPossibleDuplicate())
        .build();
  }

  public static List<ExtractedIngredientDto> map(List<ExtractedIngredient> extractedIngredients) {
    return extractedIngredients.stream()
        .map(ExtractedIngredientMapper::map)
        .collect(Collectors.toList());
  }
}
