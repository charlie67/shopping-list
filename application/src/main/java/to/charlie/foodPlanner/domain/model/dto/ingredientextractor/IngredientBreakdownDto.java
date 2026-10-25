package to.charlie.foodPlanner.domain.model.dto.ingredientextractor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IngredientBreakdownDto(
				@JsonProperty("ingredient")
				List<IngredientItemDto> ingredients,
				String original,
				String preparation,
				@JsonProperty("quantity")
				List<QuantityItemDto> quantities,
				String size,
				String comment,
				String purpose
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record IngredientItemDto(String name) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record QuantityItemDto(Double quantity, Double quantityMax, String text, String unit) {

	}
}

