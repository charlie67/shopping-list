package to.charlie.foodPlanner.domain.model.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FrozenMealDto {

	private UUID id;

	private UUID batchId;

	private UUID recipeId;

	private String recipeName;

	private String recipeImageUrl;

	private LocalDate cookedOn;
}
