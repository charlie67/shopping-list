package to.charlie.foodPlanner.domain.model.internal.plan;

/**
 * Where one meal from a batch has ended up. A meal that has been given no home at all is not a row
 * at all - the spare pool is derived as the batch's meals_total minus the rows it holds.
 */
public enum MealPlacement {
	COOK,
	LEFTOVER,
	FROZEN
}
