package to.charlie.foodplanner.domain.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;


/**
 * A DTO for the {@link to.charlie.foodplanner.domain.entity.DayPlannerEntity} entity
 */
public record DayPlannerDto(Date day, Set<RecipeDto> recipes) implements Serializable
{
}