package to.charlie.foodPlanner.domain.extraction.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PluralityDependentDto(
    Integer start,
    Integer end,
    String plural,
    String singular
) {

}