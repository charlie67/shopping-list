package to.charlie.foodPlanner.domain.model.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

@Getter
@Setter
@AllArgsConstructor
@Builder
public final class IngredientDto {

  private final String ingredientName;
  private final double quantity;
  private final MeasurementUnit unit;
  private final UUID id;

}
