/**
 * Calendar dates for the meal planner. Every date in this feature is a day with no time, so the
 * rules here are narrow on purpose:
 *
 *  - Build dates with `new Date(y, m, d)`. `new Date('2026-09-22')` parses as UTC midnight and
 *    renders as the 21st anywhere west of Greenwich.
 *  - Format them with `toIsoDate`. `toISOString()` converts to UTC and shifts the day the other
 *    way east of it.
 *
 * Either mistake produces a planner that is a day out, works all summer, and breaks the week the
 * clocks change. Nothing in `features/plan` should call the `Date` constructor or `toISOString`
 * directly.
 */

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

// Monday first, matching getDay() shifted by one. toLocaleString is avoided throughout: it renders
// September as "Sept" in current browsers, which does not match the rest of the app.
const DAY_NAMES = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

const MS_PER_DAY = 24 * 60 * 60 * 1000;

const pad = (n: number) => String(n).padStart(2, '0');

/** The local calendar date as `YYYY-MM-DD`. */
export function toIsoDate(date: Date): string {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** `YYYY-MM-DD` as local midnight. */
export function fromIsoDate(iso: string): Date {
    const [year, month, day] = iso.split('-').map(Number);
    return new Date(year, month - 1, day);
}

export function addDays(date: Date, days: number): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
}

export function addWeeks(date: Date, weeks: number): Date {
    return addDays(date, weeks * 7);
}

/**
 * Whole calendar days from `a` to `b`, negative when `b` is earlier. Compared as UTC midnights
 * because a day spanning a clock change is 23 or 25 hours long, which rounds the wrong way.
 */
export function daysBetween(a: Date, b: Date): number {
    const utcA = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
    const utcB = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());
    return Math.round((utcB - utcA) / MS_PER_DAY);
}

/** The Monday of the week containing `date`. */
export function startOfWeek(date: Date): Date {
    const day = date.getDay();
    // getDay() is 0 for Sunday, which belongs to the week that started six days earlier.
    const daysSinceMonday = day === 0 ? 6 : day - 1;
    return addDays(date, -daysSinceMonday);
}

export function weekDays(weekStart: Date): Date[] {
    return Array.from({length: 7}, (_, i) => addDays(weekStart, i));
}

export function dayName(date: Date): string {
    const day = date.getDay();
    return DAY_NAMES[day === 0 ? 6 : day - 1];
}

export function monthName(date: Date): string {
    return MONTHS[date.getMonth()];
}

export function isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear()
        && a.getMonth() === b.getMonth()
        && a.getDate() === b.getDate();
}

/** `"Mon 22 – Sun 28 Sep"`, naming both months only when the week spans two. */
export function formatWeekRange(weekStart: Date): string {
    const end = addDays(weekStart, 6);
    const startMonth = monthName(weekStart);
    const endMonth = monthName(end);
    const start = startMonth === endMonth
        ? `Mon ${weekStart.getDate()}`
        : `Mon ${weekStart.getDate()} ${startMonth}`;
    return `${start} – Sun ${end.getDate()} ${endMonth}`;
}

/** `"Mon 15"` — the form used on the planner's chips. */
export function formatDayChip(iso: string): string {
    const date = fromIsoDate(iso);
    return `${dayName(date)} ${date.getDate()}`;
}

/**
 * How many whole weeks `weekStart` is from the week containing `today`.
 *
 * Both sides are snapped to their Monday, so this counts weeks apart rather than dividing a day gap.
 * Passing a mid-week date otherwise returned a fraction, which `weekLabel` rendered verbatim as
 * "In 0.8571428571428571 weeks".
 */
export function weeksFromCurrent(weekStart: Date, today: Date): number {
    return daysBetween(startOfWeek(today), startOfWeek(weekStart)) / 7;
}

export function weekLabel(weekStart: Date, today: Date): string {
    const offset = weeksFromCurrent(weekStart, today);
    if (offset === 0) return 'This week';
    if (offset === 1) return 'Next week';
    if (offset === -1) return 'Last week';
    return offset > 0 ? `In ${offset} weeks` : `${-offset} weeks ago`;
}
