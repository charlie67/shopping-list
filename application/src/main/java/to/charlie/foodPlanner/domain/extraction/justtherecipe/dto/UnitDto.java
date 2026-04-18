package to.charlie.foodPlanner.domain.extraction.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UnitDto(
    Integer start,
    Integer end,
    String id,
    @JsonProperty("display_type") String displayType,
    Integer item
) {

}