package to.charlie.foodPlanner.domain.extraction.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemDto(
    Double density,
    String state
) {

}