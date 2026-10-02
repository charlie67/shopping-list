import {describe, expect, it} from 'vitest';
import {
    addDays,
    addWeeks,
    daysBetween,
    formatDayChip,
    formatWeekRange,
    fromIsoDate,
    isSameDay,
    startOfWeek,
    toIsoDate,
    weekDays,
    weekLabel,
    weeksFromCurrent,
} from './week';

describe('toIsoDate', () => {
    it('formats the local calendar date, not the UTC one', () => {
        // 23:30 local on the 22nd is already the 23rd in UTC. toISOString would say so; this must not.
        const lateEvening = new Date(2026, 8, 22, 23, 30);
        expect(toIsoDate(lateEvening)).toBe('2026-09-22');
    });

    it('pads single-digit months and days', () => {
        expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
    });
});

describe('fromIsoDate', () => {
    it('reads a date as local midnight, keeping the day it names', () => {
        const date = fromIsoDate('2026-09-22');
        expect(date.getDate()).toBe(22);
        expect(date.getMonth()).toBe(8);
        expect(date.getFullYear()).toBe(2026);
        expect(date.getHours()).toBe(0);
    });

    it('round-trips with toIsoDate', () => {
        for (const iso of ['2026-01-01', '2026-06-15', '2026-10-25', '2026-12-31']) {
            expect(toIsoDate(fromIsoDate(iso))).toBe(iso);
        }
    });
});

describe('startOfWeek', () => {
    it('returns the Monday of a midweek day', () => {
        expect(toIsoDate(startOfWeek(fromIsoDate('2026-09-24')))).toBe('2026-09-21');
    });

    it('treats Sunday as the end of the week that began six days earlier', () => {
        // The classic off-by-one: getDay() is 0 for Sunday, which is not the start of anything.
        expect(toIsoDate(startOfWeek(fromIsoDate('2026-09-27')))).toBe('2026-09-21');
    });

    it('leaves a Monday where it is', () => {
        expect(toIsoDate(startOfWeek(fromIsoDate('2026-09-21')))).toBe('2026-09-21');
    });
});

describe('addDays and addWeeks', () => {
    it('crosses a month end', () => {
        expect(toIsoDate(addDays(fromIsoDate('2026-09-29'), 3))).toBe('2026-10-02');
    });

    it('crosses a year end', () => {
        expect(toIsoDate(addWeeks(fromIsoDate('2026-12-28'), 1))).toBe('2027-01-04');
    });

    it('handles the clocks going back without losing or gaining a day', () => {
        // BST ends on Sunday 25 October 2026, making that day 25 hours long.
        expect(toIsoDate(addWeeks(fromIsoDate('2026-10-19'), 1))).toBe('2026-10-26');
        expect(toIsoDate(addDays(fromIsoDate('2026-10-24'), 2))).toBe('2026-10-26');
    });

    it('handles the clocks going forward', () => {
        // BST starts on Sunday 29 March 2026, making that day 23 hours long.
        expect(toIsoDate(addWeeks(fromIsoDate('2026-03-23'), 1))).toBe('2026-03-30');
    });
});

describe('daysBetween', () => {
    it('counts whole calendar days', () => {
        expect(daysBetween(fromIsoDate('2026-09-21'), fromIsoDate('2026-09-28'))).toBe(7);
    });

    it('is negative going backwards', () => {
        expect(daysBetween(fromIsoDate('2026-09-28'), fromIsoDate('2026-09-21'))).toBe(-7);
    });

    it('counts seven days across the end of BST, not 7.04', () => {
        // The week containing 25 Oct 2026 is 169 hours long; a naive ms division rounds it wrong.
        expect(daysBetween(fromIsoDate('2026-10-19'), fromIsoDate('2026-10-26'))).toBe(7);
    });
});

describe('weekDays', () => {
    it('returns Monday to Sunday', () => {
        const days = weekDays(fromIsoDate('2026-09-21')).map(toIsoDate);
        expect(days).toEqual([
            '2026-09-21', '2026-09-22', '2026-09-23', '2026-09-24',
            '2026-09-25', '2026-09-26', '2026-09-27',
        ]);
    });

    it('still returns seven distinct days across a clock change', () => {
        const days = weekDays(fromIsoDate('2026-10-19')).map(toIsoDate);
        expect(new Set(days).size).toBe(7);
        expect(days[6]).toBe('2026-10-25');
    });
});

describe('formatWeekRange', () => {
    it('names the month once when the week sits inside one', () => {
        expect(formatWeekRange(fromIsoDate('2026-09-21'))).toBe('Mon 21 – Sun 27 Sep');
    });

    it('names both months when the week spans two', () => {
        expect(formatWeekRange(fromIsoDate('2026-09-28'))).toBe('Mon 28 Sep – Sun 4 Oct');
    });

    it('uses Sep rather than the browser locale\'s Sept', () => {
        expect(formatWeekRange(fromIsoDate('2026-09-21'))).toContain('Sep');
        expect(formatWeekRange(fromIsoDate('2026-09-21'))).not.toContain('Sept');
    });
});

describe('weekLabel', () => {
    const today = fromIsoDate('2026-09-23');   // a Wednesday

    it('names the nearby weeks', () => {
        expect(weekLabel(fromIsoDate('2026-09-21'), today)).toBe('This week');
        expect(weekLabel(fromIsoDate('2026-09-28'), today)).toBe('Next week');
        expect(weekLabel(fromIsoDate('2026-09-14'), today)).toBe('Last week');
    });

    it('counts the further ones', () => {
        expect(weekLabel(fromIsoDate('2026-10-12'), today)).toBe('In 3 weeks');
        expect(weekLabel(fromIsoDate('2026-09-07'), today)).toBe('2 weeks ago');
    });

    it('still counts whole weeks across a clock change', () => {
        expect(weeksFromCurrent(fromIsoDate('2026-10-26'), fromIsoDate('2026-10-21'))).toBe(1);
    });

    // A control handing over a plain date rather than its Monday used to divide a day gap by seven
    // and label the current week "In 0.8571428571428571 weeks".
    it('counts whole weeks from a date that is not a Monday', () => {
        const sunday = fromIsoDate('2026-09-27');
        expect(weeksFromCurrent(sunday, today)).toBe(0);
        expect(weekLabel(sunday, today)).toBe('This week');
        expect(weekLabel(fromIsoDate('2026-09-30'), today)).toBe('Next week');
        expect(weekLabel(fromIsoDate('2026-09-16'), today)).toBe('Last week');
    });

    it('never labels a week with a fraction', () => {
        for (let offset = -21; offset <= 21; offset++) {
            const label = weekLabel(addDays(today, offset), today);
            expect(label).not.toMatch(/\d\./);
        }
    });
});

describe('formatDayChip', () => {
    it('is the day name and date', () => {
        expect(formatDayChip('2026-09-21')).toBe('Mon 21');
        expect(formatDayChip('2026-09-27')).toBe('Sun 27');
    });
});

describe('isSameDay', () => {
    it('ignores the time of day', () => {
        expect(isSameDay(new Date(2026, 8, 22, 0, 1), new Date(2026, 8, 22, 23, 59))).toBe(true);
        expect(isSameDay(fromIsoDate('2026-09-22'), fromIsoDate('2026-09-23'))).toBe(false);
    });
});
