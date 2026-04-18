package to.charlie.foodPlanner.domain.extraction.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuantityDto(
    Integer start,
    Integer end,
    Double value,
    Integer unit,
    @JsonProperty("plurality_dependents") List<PluralityDependentDto> pluralityDependents
) {

}