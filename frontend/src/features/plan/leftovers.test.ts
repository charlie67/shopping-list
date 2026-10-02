import {describe, expect, it} from 'vitest';
import {defaultLeftovers, type LeftoverChoice} from './leftovers';
import {addDays as addDaysToDate, fromIsoDate, toIsoDate, weekDays} from '@/common/date/week';

const MONDAY = '2026-09-21';

const addDays = (iso: string, days: number) => toIsoDate(addDaysToDate(fromIsoDate(iso), days));
const weekOf = (iso: string) => weekDays(fromIsoDate(iso)).map(toIsoDate);

const plan = (overrides: Partial<Parameters<typeof defaultLeftovers>[0]> = {}) =>
    defaultLeftovers({
        previous: [],
        leftoverCount: 2,
        cookDate: MONDAY,
        isFree: () => true,
        weekDays: weekOf(MONDAY),
        addDays,
        ...overrides,
    });

const days = (choices: LeftoverChoice[]) =>
    choices.map((choice) => (choice.mode === 'day' ? choice.plannedDate : choice.mode));

describe('defaultLeftovers', () => {
    it('fills the free nights just after the cook', () => {
        expect(days(plan())).toEqual(['2026-09-22', '2026-09-23']);
    });

    it('skips nights that are already busy', () => {
        const busy = new Set(['2026-09-22', '2026-09-24']);
        expect(days(plan({isFree: (iso) => !busy.has(iso)})))
            .toEqual(['2026-09-23', '2026-09-25']);
    });

    it('falls back to the freezer once the week runs out', () => {
        // Cooking on the Saturday leaves only the Sunday before the week ends.
        expect(days(plan({cookDate: '2026-09-26', leftoverCount: 3})))
            .toEqual(['2026-09-27', 'freeze', 'freeze']);
    });

    it('leaves every meal spare when there is no cook night', () => {
        expect(days(plan({cookDate: null, leftoverCount: 2}))).toEqual(['skip', 'skip']);
    });

    it('keeps the choices already made', () => {
        const previous: LeftoverChoice[] = [{mode: 'freeze'}, {mode: 'skip'}];
        expect(plan({previous})).toBe(previous);
    });

    it('keeps a chosen day that is still after the cook night', () => {
        const previous: LeftoverChoice[] = [
            {mode: 'day', plannedDate: '2026-09-25'},
            {mode: 'freeze'},
        ];
        expect(plan({previous})).toBe(previous);
    });

    // The bug that week navigation made easy to hit: the cook moves, the leftover does not, and the
    // server refuses a meal eaten before it was cooked.
    it('re-places a leftover the cook night has moved past', () => {
        const previous: LeftoverChoice[] = [
            {mode: 'day', plannedDate: '2026-09-22'},
            {mode: 'day', plannedDate: '2026-09-26'},
        ];
        expect(days(plan({previous, cookDate: '2026-09-24'})))
            .toEqual(['2026-09-25', '2026-09-26']);
    });

    it('re-places a leftover left behind in an earlier week', () => {
        const previous: LeftoverChoice[] = [{mode: 'day', plannedDate: '2026-09-23'}];
        const nextMonday = '2026-09-28';
        expect(days(plan({
            previous,
            leftoverCount: 1,
            cookDate: nextMonday,
            weekDays: weekOf(nextMonday),
        }))).toEqual(['2026-09-29']);
    });

    it('never places a leftover on or before the cook night', () => {
        for (let offset = 0; offset < 7; offset += 1) {
            const cookDate = addDays(MONDAY, offset);
            const previous: LeftoverChoice[] = [{mode: 'day', plannedDate: MONDAY}];
            const choices = plan({previous, leftoverCount: 3, cookDate});
            for (const choice of choices) {
                if (choice.mode === 'day') expect(choice.plannedDate > cookDate).toBe(true);
            }
        }
    });

    it('grows and shrinks with the meal count', () => {
        const two = plan({leftoverCount: 2});
        expect(days(plan({previous: two, leftoverCount: 3})))
            .toEqual(['2026-09-22', '2026-09-23', '2026-09-24']);
        expect(days(plan({previous: two, leftoverCount: 1}))).toEqual(['2026-09-22']);
        expect(plan({previous: two, leftoverCount: 0})).toEqual([]);
    });
});
