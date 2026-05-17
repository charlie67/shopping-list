package to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IngredientDto(
				String name,
				List<ItemDto> items,
				List<QuantityDto> quantities,
				List<UnitDto> units,
				List<Object> sizes,
				String type
) {

}