import {describe, expect, it} from 'vitest';
import type {PlanBatchDto, PlannedMealDto} from '@/common/types/plan';
import {
    type DragPayload,
    dropTargetId,
    parseDropTargetId,
    resolveDrop,
} from './plannerDnd';

let nextId = 0;
const meal = (placement: PlannedMealDto['placement'], plannedDate: string | null): PlannedMealDto =>
    ({id: `meal-${nextId++}`, placement, plannedDate});

const batch = (meals: PlannedMealDto[], name = 'Chilli'): PlanBatchDto => ({
    id: `batch-${nextId++}`,
    recipeId: 'recipe-1',
    recipeName: name,
    recipeImageUrl: null,
    recipeYield: '6',
    servings: 6,
    cookingFor: 2,
    mealsTotal: 3,
    spareMeals: 1,
    meals,
});

describe('dropping onto a day', () => {
    it('moves a leftover to a later night', () => {
        const cook = meal('COOK', '2026-09-21');
        const leftover = meal('LEFTOVER', '2026-09-23');
        const chilli = batch([cook, leftover]);
        const payload: DragPayload = {
            kind: 'meal', mealId: leftover.id, placement: 'LEFTOVER', batch: chilli,
        };

        expect(resolveDrop(payload, {kind: 'day', isoDate: '2026-09-25'})).toEqual({
            action: 'move', mealId: leftover.id, placement: 'LEFTOVER', plannedDate: '2026-09-25',
        });
    });

    it('refuses a leftover before its cook night, naming the recipe', () => {
        const cook = meal('COOK', '2026-09-23');
        const leftover = meal('LEFTOVER', '2026-09-25');
        const chilli = batch([cook, leftover]);
        const outcome = resolveDrop(
            {kind: 'meal', mealId: leftover.id, placement: 'LEFTOVER', batch: chilli},
            {kind: 'day', isoDate: '2026-09-21'},
        );

        expect(outcome.action).toBe('refuse');
        expect(outcome).toHaveProperty('reason', expect.stringContaining('Chilli'));
    });

    it('refuses the cook night itself as a leftover day', () => {
        const cook = meal('COOK', '2026-09-23');
        const leftover = meal('LEFTOVER', '2026-09-25');
        const chilli = batch([cook, leftover]);

        expect(resolveDrop(
            {kind: 'meal', mealId: leftover.id, placement: 'LEFTOVER', batch: chilli},
            {kind: 'day', isoDate: '2026-09-23'},
        ).action).toBe('refuse');
    });

    it('lets a frozen meal land on any day, since it was cooked weeks ago', () => {
        const payload: DragPayload = {
            kind: 'frozen', mealId: 'frozen-1', recipeId: 'recipe-1', recipeName: 'Ragu',
        };

        expect(resolveDrop(payload, {kind: 'day', isoDate: '2026-09-21'})).toEqual({
            action: 'move', mealId: 'frozen-1', placement: 'LEFTOVER', plannedDate: '2026-09-21',
        });
    });

    it('places a spare meal as a leftover', () => {
        const chilli = batch([meal('COOK', '2026-09-21')]);

        expect(resolveDrop({kind: 'spare', batch: chilli}, {kind: 'day', isoDate: '2026-09-24'}))
            .toEqual({
                action: 'place', batchId: chilli.id, placement: 'LEFTOVER', plannedDate: '2026-09-24',
            });
    });

    it('treats the first meal of a queued batch as choosing its cook night', () => {
        const queued = batch([]);

        expect(resolveDrop({kind: 'spare', batch: queued}, {kind: 'day', isoDate: '2026-09-24'}))
            .toEqual({
                action: 'place', batchId: queued.id, placement: 'COOK', plannedDate: '2026-09-24',
            });
    });

    it('moves a cook night anywhere, including earlier', () => {
        const cook = meal('COOK', '2026-09-23');
        const chilli = batch([cook]);

        expect(resolveDrop(
            {kind: 'meal', mealId: cook.id, placement: 'COOK', batch: chilli},
            {kind: 'day', isoDate: '2026-09-21'},
        )).toEqual({action: 'move', mealId: cook.id, placement: 'COOK', plannedDate: '2026-09-21'});
    });

    it('refuses a no-op drop back onto the same night', () => {
        const cook = meal('COOK', '2026-09-23');
        const chilli = batch([cook]);

        expect(resolveDrop(
            {kind: 'meal', mealId: cook.id, placement: 'COOK', batch: chilli},
            {kind: 'day', isoDate: '2026-09-23'},
        ).action).toBe('refuse');
    });
});

describe('dropping into the freezer', () => {
    it('freezes a placed leftover', () => {
        const leftover = meal('LEFTOVER', '2026-09-23');
        const chilli = batch([meal('COOK', '2026-09-21'), leftover]);

        expect(resolveDrop(
            {kind: 'meal', mealId: leftover.id, placement: 'LEFTOVER', batch: chilli},
            {kind: 'freezer'},
        )).toEqual({action: 'move', mealId: leftover.id, placement: 'FROZEN', plannedDate: null});
    });

    it('freezes a spare meal', () => {
        const chilli = batch([meal('COOK', '2026-09-21')]);

        expect(resolveDrop({kind: 'spare', batch: chilli}, {kind: 'freezer'})).toEqual({
            action: 'place', batchId: chilli.id, placement: 'FROZEN', plannedDate: null,
        });
    });

    it('refuses to freeze the cook night, which is what makes the meals', () => {
        const cook = meal('COOK', '2026-09-21');
        const chilli = batch([cook]);
        const outcome = resolveDrop(
            {kind: 'meal', mealId: cook.id, placement: 'COOK', batch: chilli},
            {kind: 'freezer'},
        );

        expect(outcome.action).toBe('refuse');
        expect(outcome).toHaveProperty('reason', expect.stringContaining('Chilli'));
    });

    it('refuses to freeze from a batch that has not been cooked yet', () => {
        const queued = batch([]);

        expect(resolveDrop({kind: 'spare', batch: queued}, {kind: 'freezer'}).action).toBe('refuse');
    });
});

describe('dropping onto the pool', () => {
    it('takes a placed meal off its day', () => {
        const leftover = meal('LEFTOVER', '2026-09-23');
        const chilli = batch([meal('COOK', '2026-09-21'), leftover]);

        expect(resolveDrop(
            {kind: 'meal', mealId: leftover.id, placement: 'LEFTOVER', batch: chilli},
            {kind: 'pool'},
        )).toEqual({action: 'unplace', mealId: leftover.id});
    });

    it('takes a meal out of the freezer', () => {
        expect(resolveDrop(
            {kind: 'frozen', mealId: 'frozen-1', recipeId: 'r', recipeName: 'Ragu'},
            {kind: 'pool'},
        )).toEqual({action: 'unplace', mealId: 'frozen-1'});
    });

    it('refuses a spare, which is already there', () => {
        expect(resolveDrop({kind: 'spare', batch: batch([])}, {kind: 'pool'}).action).toBe('refuse');
    });
});

describe('drop target ids', () => {
    it('round-trip', () => {
        for (const target of [
            {kind: 'day', isoDate: '2026-09-21'},
            {kind: 'freezer'},
            {kind: 'pool'},
        ] as const) {
            expect(parseDropTargetId(dropTargetId(target))).toEqual(target);
        }
    });

    it('ignores anything else', () => {
        expect(parseDropTargetId('something-else')).toBeNull();
    });
});
