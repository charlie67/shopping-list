package to.charlie.foodPlanner.domain.extraction.justtherecipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InstructionDto(
    String text,
    String type
) {

}