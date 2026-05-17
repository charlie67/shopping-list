package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractedRecipeIngredient {

	private String fullText;
	private String ingredientName;
	private Double quantity;
	private String quantityText;

	private String unitText;
	private MeasurementUnit unit;
	private String size;
	private String preparation;
	private String comment;
	private String purpose;
}
