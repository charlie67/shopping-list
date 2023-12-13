package to.charlie.foodPlanner.domain.model.dto.extraction;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ExtractedRecipeDto {

  String url;

  String recipeName;

  List<ExtractedRecipeStepsDto> instructions;

  List<ExtractedIngredientDto> ingredients;
}
