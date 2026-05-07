package to.charlie.foodPlanner.domain.model.dto.ingredientextractor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IngredientBreakdownDto(
				@JsonProperty("ingredient")
				List<IngredientItemDto> ingredients,
				String original,
				String preparation,
				@JsonProperty("quantity")
				List<QuantityItemDto> quantities,
				String size
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record IngredientItemDto(String name) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record QuantityItemDto(Double quantity, String text, String unit) {

	}
}

