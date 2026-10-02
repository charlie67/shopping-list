/**
 * Where a new batch's leftover meals go, before it has been planned at all.
 *
 * Pure so it can be tested: this decides what the plan sheet submits, and a leftover that ends up on
 * or before its cook night is refused by the server with nothing the user can act on.
 */

/** Where one of the batch's leftover meals is going. "skip" leaves it spare in the pool. */
export type LeftoverChoice =
    | { mode: 'day'; plannedDate: string }
    | { mode: 'freeze' }
    | { mode: 'skip' };

interface Options {
    /** The choices already on screen, kept wherever they are still legal. */
    previous: LeftoverChoice[];
    /** How many meals the pot makes beyond the cook night itself. */
    leftoverCount: number;
    /** The chosen cook night, or null when the batch is being parked in the queue. */
    cookDate: string | null;
    /** Whether that day has nothing else cooked or eaten on it yet. */
    isFree: (iso: string) => boolean;
    /** The days of the cook night's own week, which is as far as a default will reach. */
    weekDays: string[];
    /** `iso` shifted by `days`, injected so this module owns no date arithmetic. */
    addDays: (iso: string, days: number) => string;
}

/**
 * Fills every leftover slot, preferring the free nights just after the cook and falling back to the
 * freezer when the week runs out.
 *
 * Choices the user has already made are kept, with one exception: a day that is no longer after the
 * cook night is re-placed, because moving the cook forward — or into another week — strands it.
 * Returns `previous` itself when nothing needs to change, so React state is left untouched.
 */
export function defaultLeftovers(options: Options): LeftoverChoice[] {
    const {previous, leftoverCount, cookDate, isFree, weekDays, addDays} = options;

    if (cookDate === null) {
        // Nothing is cooked yet, so nothing can be eaten or frozen from it.
        return Array.from({length: leftoverCount}, () => ({mode: 'skip'}));
    }

    const kept = Array.from({length: leftoverCount}, (_, index): LeftoverChoice | null => {
        const choice = previous[index];
        if (choice === undefined) return null;
        return choice.mode === 'day' && choice.plannedDate <= cookDate ? null : choice;
    });

    if (kept.every((choice) => choice !== null)) {
        return previous.length === leftoverCount ? previous : kept as LeftoverChoice[];
    }

    const taken = new Set(kept.flatMap(
        (choice) => (choice?.mode === 'day' ? [choice.plannedDate] : [])));

    return kept.map((choice) => {
        if (choice !== null) return choice;
        for (let offset = 1; offset <= 6; offset += 1) {
            const iso = addDays(cookDate, offset);
            if (isFree(iso) && !taken.has(iso) && weekDays.includes(iso)) {
                taken.add(iso);
                return {mode: 'day', plannedDate: iso};
            }
        }
        return {mode: 'freeze'};
    });
}
