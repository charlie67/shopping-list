package to.charlie.foodPlanner.domain.model.dto.plan;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

/**
 * Covers every placement gesture: a chip moved to another day, the freezer to a day, a day to the
 * freezer, and moving a cook night.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedMealUpdateDto {

  private MealPlacement placement;

  private LocalDate plannedDate;
}
