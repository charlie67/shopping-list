/** Where one meal from a batch has ended up. A meal with no home at all is not a row: see PlanBatchDto. */
export type MealPlacement = 'COOK' | 'LEFTOVER' | 'FROZEN';

export interface PlannedMealDto {
    id: string;
    placement: MealPlacement;
    // Null only for FROZEN. A cook night or a leftover always sits on a day.
    plannedDate: string | null;
}

export interface PlanBatchDto {
    id: string;
    recipeId: string;
    // Denormalised by the backend so the planner renders without fetching a recipe that may be
    // hundreds of rows deep in the paginated grid.
    recipeName: string;
    recipeImageUrl: string | null;
    recipeYield: string | null;
    // recipeYield read as a number, null when the scraped text held nothing usable.
    servings: number | null;
    // The household when this batch was planned, kept so the row can explain an old figure.
    cookingFor: number;
    // How many meals this pot makes. A snapshot taken at plan time, deliberately not a live
    // division of servings by the current household - see docs. Editing a recipe's yield or
    // changing the household must not rewrite a leftover that has already been eaten.
    mealsTotal: number;
    // mealsTotal minus the meals that have been given a home. Computed by the backend, and always
    // zero for a queued batch: nothing can be placed before there is a cook night to follow.
    spareMeals: number;
    meals: PlannedMealDto[];
}

export interface FrozenMealDto {
    id: string;
    batchId: string;
    recipeId: string;
    // Carried on the row because a frozen meal's batch usually belongs to some other week, so the
    // freezer panel can render without cross-referencing.
    recipeName: string;
    recipeImageUrl: string | null;
    cookedOn: string | null;
}

export interface PlanWeekDto {
    weekStart: string;
    weekEnd: string;
    // The current PLAN_PORTION_SIZE, echoed so one request boots the page.
    cookingFor: number;
    batches: PlanBatchDto[];
    // Both global rather than week-scoped, so every week response carries the same truth for them.
    queue: PlanBatchDto[];
    freezer: FrozenMealDto[];
}

export interface PlanLeftoverRequest {
    placement: Exclude<MealPlacement, 'COOK'>;
    plannedDate?: string | null;
}

export interface PlanCreateDto {
    recipeId: string;
    // Null parks the batch in the queue, in which case it may carry no leftovers at all.
    cookDate: string | null;
    cookingFor?: number;
    // Only sent when the user set it by hand, which happens when servings could not be read.
    mealsTotal?: number;
    leftovers: PlanLeftoverRequest[];
}

/** Giving one of a batch's spare meals a home. A spare has no row until it is placed. */
export interface PlannedMealCreateDto {
    placement: MealPlacement;
    plannedDate?: string | null;
}

export interface PlannedMealUpdateDto {
    placement: MealPlacement;
    plannedDate: string | null;
}
