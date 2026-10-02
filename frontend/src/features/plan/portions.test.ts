import {describe, expect, it} from 'vitest';
import type {PlanBatchDto, PlannedMealDto} from '@/common/types/plan';
import {
    canPlaceLeftoverOn,
    coverage,
    cookDateOf,
    isQueued,
    mealsForDay,
    orderedMeals,
    pipsFor,
    spareTotal,
} from './portions';

let nextId = 0;
const meal = (placement: PlannedMealDto['placement'], plannedDate: string | null): PlannedMealDto =>
    ({id: `meal-${nextId++}`, placement, plannedDate});

const batch = (overrides: Partial<PlanBatchDto> = {}): PlanBatchDto => ({
    id: `batch-${nextId++}`,
    recipeId: 'recipe-1',
    recipeName: 'My best chilli con carne',
    recipeImageUrl: null,
    recipeYield: '6',
    servings: 6,
    cookingFor: 2,
    mealsTotal: 3,
    spareMeals: 0,
    meals: [],
    ...overrides,
});

describe('isQueued', () => {
    it('is true for a batch with no cook night', () => {
        expect(isQueued(batch({meals: []}))).toBe(true);
    });

    it('is false once it has one', () => {
        expect(isQueued(batch({meals: [meal('COOK', '2026-09-21')]}))).toBe(false);
    });
});

describe('pipsFor', () => {
    it('shows the cook, its placed leftovers and the spares', () => {
        const chilli = batch({
            mealsTotal: 3,
            spareMeals: 1,
            meals: [meal('COOK', '2026-09-21'), meal('LEFTOVER', '2026-09-23')],
        });
        expect(pipsFor(chilli)).toEqual(['cook', 'leftover', 'spare']);
    });

    it('marks frozen portions apart from placed leftovers', () => {
        const ragu = batch({
            mealsTotal: 4,
            spareMeals: 1,
            meals: [
                meal('COOK', '2026-09-21'),
                meal('FROZEN', null),
                meal('LEFTOVER', '2026-09-24'),
            ],
        });
        // Ordered cook, then what is eaten, then what is put by — regardless of the order sent.
        expect(pipsFor(ragu)).toEqual(['cook', 'leftover', 'frozen', 'spare']);
    });

    it('shows a queued batch entirely as outlines', () => {
        // The meals exist but none can be placed: there is no cook night for them to follow.
        const queued = batch({mealsTotal: 3, spareMeals: 0, meals: []});
        expect(pipsFor(queued)).toEqual(['spare', 'spare', 'spare']);
    });

    it('never renders more pips than the batch makes', () => {
        const chilli = batch({
            mealsTotal: 2,
            spareMeals: 0,
            meals: [meal('COOK', '2026-09-21'), meal('LEFTOVER', '2026-09-23')],
        });
        expect(pipsFor(chilli)).toHaveLength(2);
    });
});

describe('spareTotal', () => {
    it('adds up the unplaced meals', () => {
        expect(spareTotal([
            batch({spareMeals: 2, meals: [meal('COOK', '2026-09-21')]}),
            batch({spareMeals: 1, meals: [meal('COOK', '2026-09-22')]}),
        ])).toBe(3);
    });

    it('counts nothing for a queued batch, whose meals cannot be spent yet', () => {
        expect(spareTotal([batch({mealsTotal: 4, spareMeals: 0, meals: []})])).toBe(0);
    });
});

describe('mealsForDay', () => {
    it('finds every meal on a day, across batches', () => {
        const chilli = batch({meals: [meal('COOK', '2026-09-21')]});
        const salad = batch({recipeName: 'Halloumi salad', meals: [meal('COOK', '2026-09-21')]});
        expect(mealsForDay([chilli, salad], '2026-09-21')).toHaveLength(2);
    });

    it('ignores frozen meals, which sit on no day', () => {
        const ragu = batch({meals: [meal('COOK', '2026-09-21'), meal('FROZEN', null)]});
        expect(mealsForDay([ragu], '2026-09-21')).toHaveLength(1);
    });
});

describe('coverage', () => {
    const week = ['2026-09-21', '2026-09-22', '2026-09-23'];

    it('counts a night as cooked when anything is cooked on it', () => {
        const chilli = batch({meals: [meal('COOK', '2026-09-21'), meal('LEFTOVER', '2026-09-23')]});
        expect(coverage([chilli], week)).toEqual({cooked: 1, leftovers: 1, empty: 1});
    });

    it('counts a night with both a cook and a leftover once, as cooked', () => {
        const chilli = batch({meals: [meal('COOK', '2026-09-21')]});
        const ragu = batch({recipeName: 'Ragu', meals: [meal('LEFTOVER', '2026-09-21')]});
        expect(coverage([chilli, ragu], week)).toEqual({cooked: 1, leftovers: 0, empty: 2});
    });

    it('counts an empty week as entirely unplanned', () => {
        expect(coverage([], week)).toEqual({cooked: 0, leftovers: 0, empty: 3});
    });
});

describe('canPlaceLeftoverOn', () => {
    const chilli = batch({meals: [meal('COOK', '2026-09-21')]});

    it('allows a day after the cook night', () => {
        expect(canPlaceLeftoverOn(chilli, '2026-09-23')).toBe(true);
    });

    it('refuses the cook night itself and anything before it', () => {
        expect(canPlaceLeftoverOn(chilli, '2026-09-21')).toBe(false);
        expect(canPlaceLeftoverOn(chilli, '2026-09-20')).toBe(false);
    });

    it('refuses everything for a queued batch, which has not been cooked at all', () => {
        expect(canPlaceLeftoverOn(batch({meals: []}), '2026-09-23')).toBe(false);
    });
});

describe('cookDateOf and orderedMeals', () => {
    it('finds the cook night', () => {
        const chilli = batch({meals: [meal('LEFTOVER', '2026-09-23'), meal('COOK', '2026-09-21')]});
        expect(cookDateOf(chilli)).toBe('2026-09-21');
        expect(orderedMeals(chilli)[0].placement).toBe('COOK');
    });

    it('puts leftovers in the order they will be eaten', () => {
        const ragu = batch({
            meals: [
                meal('LEFTOVER', '2026-09-25'),
                meal('COOK', '2026-09-21'),
                meal('LEFTOVER', '2026-09-23'),
            ],
        });
        expect(orderedMeals(ragu).map((m) => m.plannedDate))
            .toEqual(['2026-09-21', '2026-09-23', '2026-09-25']);
    });
});
