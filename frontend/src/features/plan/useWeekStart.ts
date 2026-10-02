import {useCallback} from 'react';
import {useSearchParams} from 'react-router-dom';
import {fromIsoDate, startOfWeek, toIsoDate} from '@/common/date/week';

const WEEK_PARAM = 'week';

/**
 * The Monday of the week on screen, held in the URL so a refresh keeps your place and a week can be
 * linked to. Anything unparseable falls back to this week rather than erroring — the param is
 * user-editable.
 */
export function useWeekStart(): [string, (weekStart: string) => void] {
    const [searchParams, setSearchParams] = useSearchParams();

    const setWeekStart = useCallback((weekStart: string) => {
        setSearchParams((params) => {
            const next = new URLSearchParams(params);
            next.set(WEEK_PARAM, weekStart);
            return next;
        });
    }, [setSearchParams]);

    const thisWeek = toIsoDate(startOfWeek(new Date()));
    const param = searchParams.get(WEEK_PARAM);
    const isValid = param !== null && /^\d{4}-\d{2}-\d{2}$/.test(param);

    // Snapped, not just shape-checked, so what this returns really is a Monday however it was set -
    // a hand-edited URL, or a control that hands over a plain date. Everything downstream (the week
    // label, the range, the ribbon's seven days) treats it as one, and the backend normalises the
    // same way rather than rejecting it.
    return [isValid ? toIsoDate(startOfWeek(fromIsoDate(param))) : thisWeek, setWeekStart];
}
