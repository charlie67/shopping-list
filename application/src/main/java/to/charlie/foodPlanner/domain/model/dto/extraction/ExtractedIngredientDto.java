package to.charlie.foodPlanner.domain.model.dto.extraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ExtractedIngredientDto {

  String ingredientName;

  double quantity;

  String unit;

  String wholeName;

  boolean possibleDuplicate;
}
