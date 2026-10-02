package to.charlie.foodPlanner.domain.model.dto.plan;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

/**
 * One of a new batch's meals given a home up front. A meal the client omits is simply spare, so this
 * never carries COOK - the batch's cook night is its own field on {@link PlanCreateDto}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanLeftoverDto {

  private MealPlacement placement;

  private LocalDate plannedDate;
}
