package to.charlie.foodPlanner.domain.model.dto.extraction;

import java.util.Set;
import lombok.Builder;

@Builder
public record ExtractionRequestDto(String url, String recipeHeader, Set<String> recipeElement,
                                   String ingredientHeader, Set<String> ingredientElement) {

}
