package to.charlie.foodPlanner.domain.model.dto.plan;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanBatchDto {

  private UUID id;

  private UUID recipeId;

  /**
   * Denormalised so the planner renders without fetching a recipe that may be hundreds of rows deep
   * in the paginated grid.
   */
  private String recipeName;

  private String recipeImageUrl;

  /** The raw scraped text, for "serves 4" in the plan sheet. */
  private String recipeYield;

  /** recipeYield read as a number, null when the scraped text held nothing usable. */
  private Integer servings;

  /** The household when this batch was planned, kept so the row can explain an old figure. */
  private int cookingFor;

  /** How many meals this pot makes: a snapshot taken at plan time, not a live division. */
  private int mealsTotal;

  /**
   * mealsTotal minus the meals that have been given a home, and always zero for a queued batch:
   * nothing can be placed before there is a cook night to follow.
   */
  private int spareMeals;

  private List<PlannedMealDto> meals;
}
