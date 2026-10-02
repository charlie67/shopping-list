import type {MealPlacement, PlanBatchDto} from '@/common/types/plan';

/**
 * What a drag carries and where it can land. Drop handling routes onto the very same thunks the
 * placement sheet uses, so the rules live in one place and dragging is only ever a shortcut.
 */

export type DragPayload =
    | { kind: 'meal'; mealId: string; placement: MealPlacement; batch: PlanBatchDto }
    | { kind: 'spare'; batch: PlanBatchDto }
    | { kind: 'frozen'; mealId: string; recipeId: string; recipeName: string };

export type DropTarget =
    | { kind: 'day'; isoDate: string }
    | { kind: 'freezer' }
    | { kind: 'pool' };

export type DropOutcome =
    | { action: 'place'; batchId: string; placement: MealPlacement; plannedDate: string | null }
    | { action: 'move'; mealId: string; placement: MealPlacement; plannedDate: string | null }
    | { action: 'unplace'; mealId: string }
    | { action: 'refuse'; reason: string };

const cookDateOf = (batch: PlanBatchDto) =>
    batch.meals.find((meal) => meal.placement === 'COOK')?.plannedDate ?? null;

/**
 * Decides what a drop means, or why it cannot happen. Pure, so the rules can be tested without a
 * pointer: see the accompanying tests.
 */
export function resolveDrop(payload: DragPayload, target: DropTarget): DropOutcome {
    if (target.kind === 'pool') {
        // Only something that has a home can be sent back to the pool.
        if (payload.kind === 'spare') {
            return {action: 'refuse', reason: 'That meal is already in the pool'};
        }
        return {action: 'unplace', mealId: payload.mealId};
    }

    if (target.kind === 'freezer') {
        if (payload.kind === 'frozen') {
            return {action: 'refuse', reason: 'It is already in the freezer'};
        }
        if (payload.kind === 'meal' && payload.placement === 'COOK') {
            return {
                action: 'refuse',
                reason: `That is the night you cook ${payload.batch.recipeName} — freeze one of its spare meals instead`,
            };
        }
        if (payload.kind === 'spare' && cookDateOf(payload.batch) === null) {
            return {
                action: 'refuse',
                reason: `${payload.batch.recipeName} has not been cooked yet — give it a day first`,
            };
        }
        return payload.kind === 'spare'
            ? {action: 'place', batchId: payload.batch.id, placement: 'FROZEN', plannedDate: null}
            : {action: 'move', mealId: payload.mealId, placement: 'FROZEN', plannedDate: null};
    }

    // ------------------------------------------------------------------ onto a day
    const {isoDate} = target;

    // A meal out of the freezer was cooked in some earlier week, so no day is out of bounds.
    if (payload.kind === 'frozen') {
        return {action: 'move', mealId: payload.mealId, placement: 'LEFTOVER', plannedDate: isoDate};
    }

    const cookDate = cookDateOf(payload.batch);
    const queued = cookDate === null;
    const movingTheCook = payload.kind === 'meal' && payload.placement === 'COOK';

    // Placing the first meal of a queued batch is choosing its cook night.
    if (queued || movingTheCook) {
        if (movingTheCook && cookDate === isoDate) {
            return {action: 'refuse', reason: 'It is already cooked that night'};
        }
        return payload.kind === 'spare'
            ? {action: 'place', batchId: payload.batch.id, placement: 'COOK', plannedDate: isoDate}
            : {action: 'move', mealId: payload.mealId, placement: 'COOK', plannedDate: isoDate};
    }

    if (cookDate !== null && isoDate <= cookDate) {
        return {
            action: 'refuse',
            reason: `You have not cooked ${payload.batch.recipeName} before then`,
        };
    }

    return payload.kind === 'spare'
        ? {action: 'place', batchId: payload.batch.id, placement: 'LEFTOVER', plannedDate: isoDate}
        : {action: 'move', mealId: payload.mealId, placement: 'LEFTOVER', plannedDate: isoDate};
}

export const dropTargetId = (target: DropTarget): string =>
    target.kind === 'day' ? `day:${target.isoDate}` : target.kind;

export function parseDropTargetId(id: string): DropTarget | null {
    if (id === 'freezer' || id === 'pool') return {kind: id};
    if (id.startsWith('day:')) return {kind: 'day', isoDate: id.slice(4)};
    return null;
}
