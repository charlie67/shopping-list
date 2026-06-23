package to.charlie.foodPlanner.domain.model.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredientDto {

  private String ingredientName;
  private double quantity;
  private MeasurementUnit unit;
  private UUID id;

}
