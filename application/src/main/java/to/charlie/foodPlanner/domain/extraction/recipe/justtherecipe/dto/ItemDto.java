package to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemDto(
				Double density,
				String state
) {

}