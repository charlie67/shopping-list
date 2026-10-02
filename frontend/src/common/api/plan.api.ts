import type {
    PlanCreateDto,
    PlannedMealCreateDto,
    PlannedMealUpdateDto,
    PlanWeekDto,
} from '../types/plan';
import {PLAN_ENDPOINT} from '../constants';
import {apiDeleteJson, apiGet, apiPatch, apiPost} from './client';

// Every mutation answers with the whole week rather than the row it changed: moving a cook night
// strands leftovers, unplacing a meal changes a spare count, freezing moves a pip. Returning the
// week means the client never has to reconstruct those consequences and cannot drift.

export function getPlanWeek(weekStart: string): Promise<PlanWeekDto> {
    return apiGet<PlanWeekDto>(`${PLAN_ENDPOINT}/week/${weekStart}`);
}

export function planRecipe(plan: PlanCreateDto): Promise<PlanWeekDto> {
    return apiPost<PlanWeekDto>(PLAN_ENDPOINT, plan);
}

// Gives one of a batch's spare meals a home. A spare has no row until it is placed.
export function placeMeal(
    batchId: string,
    meal: PlannedMealCreateDto,
    weekStart: string,
): Promise<PlanWeekDto> {
    return apiPost<PlanWeekDto>(`${PLAN_ENDPOINT}/batch/${batchId}/meal?week=${weekStart}`, meal);
}

export function updateMeal(
    mealId: string,
    update: PlannedMealUpdateDto,
    weekStart: string,
): Promise<PlanWeekDto> {
    return apiPatch<PlanWeekDto>(`${PLAN_ENDPOINT}/meal/${mealId}?week=${weekStart}`, update);
}

export function unplaceMeal(mealId: string, weekStart: string): Promise<PlanWeekDto> {
    return apiDeleteJson<PlanWeekDto>(`${PLAN_ENDPOINT}/meal/${mealId}?week=${weekStart}`);
}

export function removeBatch(batchId: string, weekStart: string): Promise<PlanWeekDto> {
    return apiDeleteJson<PlanWeekDto>(`${PLAN_ENDPOINT}/batch/${batchId}?week=${weekStart}`);
}
