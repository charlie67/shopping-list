package to.charlie.foodPlanner.domain.model.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedMealDto {

	private UUID id;

	private MealPlacement placement;

	private LocalDate plannedDate;
}
