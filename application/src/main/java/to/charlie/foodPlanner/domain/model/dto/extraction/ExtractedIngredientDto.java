package to.charlie.foodPlanner.domain.model.dto.extraction;

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
public class ExtractedIngredientDto {

	private String ingredientName;

	private double quantity;

	private MeasurementUnit unit;

	private String size;

	private String preparation;

	private String purpose;

	private String comment;

	private String quantityText;

	private String fullText;

	private boolean possibleDuplicate;
}
