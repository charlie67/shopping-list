import type {PlanBatchDto, PlannedMealDto} from '@/common/types/plan';

/**
 * Everything the planner displays that is derived rather than stored: the pips on a row, the spare
 * pool, and the week's coverage. Pure functions over what the API returned, so the page never has
 * to recompute a batch's arithmetic and the two can never disagree.
 */

export type PipKind = 'cook' | 'leftover' | 'frozen' | 'spare';

/** A batch with no cook night: planned, but with no day yet. */
export function isQueued(batch: PlanBatchDto): boolean {
    return !batch.meals.some((meal) => meal.placement === 'COOK');
}

export function cookMealOf(batch: PlanBatchDto): PlannedMealDto | undefined {
    return batch.meals.find((meal) => meal.placement === 'COOK');
}

export function cookDateOf(batch: PlanBatchDto): string | null {
    return cookMealOf(batch)?.plannedDate ?? null;
}

// Cook first, then leftovers in the order they will be eaten, then the freezer.
const PLACEMENT_ORDER: Record<PlannedMealDto['placement'], number> = {
    COOK: 0,
    LEFTOVER: 1,
    FROZEN: 2,
};

export function orderedMeals(batch: PlanBatchDto): PlannedMealDto[] {
    return [...batch.meals].sort((a, b) => {
        const byPlacement = PLACEMENT_ORDER[a.placement] - PLACEMENT_ORDER[b.placement];
        if (byPlacement !== 0) return byPlacement;
        return (a.plannedDate ?? '').localeCompare(b.plannedDate ?? '');
    });
}

/**
 * One pip per meal the batch makes. A queued batch shows every meal as an outline: none of them can
 * be placed until there is a cook night for them to follow, so counting them into the spare pool
 * would advertise meals nothing can spend.
 */
export function pipsFor(batch: PlanBatchDto): PipKind[] {
    if (isQueued(batch)) {
        return Array.from({length: batch.mealsTotal}, () => 'spare' as const);
    }

    const placed: PipKind[] = orderedMeals(batch).map((meal) => {
        if (meal.placement === 'COOK') return 'cook';
        return meal.placement === 'FROZEN' ? 'frozen' : 'leftover';
    });
    const spare: PipKind[] = Array.from({length: batch.spareMeals}, () => 'spare' as const);

    return [...placed, ...spare];
}

/** Spare meals across every batch on show — the "pool" in the coverage line. */
export function spareTotal(batches: PlanBatchDto[]): number {
    return batches.reduce((total, batch) => total + (isQueued(batch) ? 0 : batch.spareMeals), 0);
}

export interface DayMeal {
    batch: PlanBatchDto;
    meal: PlannedMealDto;
}

export function mealsForDay(batches: PlanBatchDto[], isoDate: string): DayMeal[] {
    const meals: DayMeal[] = [];
    for (const batch of batches) {
        for (const meal of orderedMeals(batch)) {
            if (meal.plannedDate === isoDate) {
                meals.push({batch, meal});
            }
        }
    }
    return meals;
}

export interface Coverage {
    /** Nights with something cooked from scratch. */
    cooked: number;
    /** Nights fed only by leftovers. */
    leftovers: number;
    /** Nights with nothing on them. */
    empty: number;
}

export function coverage(batches: PlanBatchDto[], isoDates: string[]): Coverage {
    return isoDates.reduce<Coverage>((counts, isoDate) => {
        const meals = mealsForDay(batches, isoDate);
        if (meals.length === 0) {
            counts.empty += 1;
        } else if (meals.some(({meal}) => meal.placement === 'COOK')) {
            counts.cooked += 1;
        } else {
            counts.leftovers += 1;
        }
        return counts;
    }, {cooked: 0, leftovers: 0, empty: 0});
}

/**
 * Whether a leftover may be eaten on this day: there has to be a cook night before it. A meal
 * coming out of the freezer is exempt, since its batch was cooked in some earlier week.
 */
export function canPlaceLeftoverOn(batch: PlanBatchDto, isoDate: string): boolean {
    const cookDate = cookDateOf(batch);
    return cookDate !== null && isoDate > cookDate;
}
