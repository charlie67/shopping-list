package to.charlie.foodPlanner.domain.model.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanWeekDto {

	private LocalDate weekStart;

	private LocalDate weekEnd;

	private int cookingFor;

	private List<PlanBatchDto> batches;

	private List<PlanBatchDto> queue;

	private List<FrozenMealDto> freezer;
}
