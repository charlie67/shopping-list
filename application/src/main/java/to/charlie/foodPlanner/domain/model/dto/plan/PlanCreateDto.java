package to.charlie.foodPlanner.domain.model.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanCreateDto {

	private UUID recipeId;

	private LocalDate cookDate;

	private Integer cookingFor;

	private Integer mealsTotal;

	private List<PlanLeftoverDto> leftovers;
}
