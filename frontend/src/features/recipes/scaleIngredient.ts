/**
 * Scaling a recipe's quantities to cook for a different number of people.
 *
 * Nothing here changes the stored recipe: it only affects what goes on the shopping list and what a
 * planned batch shows. The recipe keeps the yield it was scraped with.
 *
 * Works on the *text* of a quantity rather than the parsed `quantity` and `unit` fields, because the
 * parsed unit is unreliable: extraction leaves it UNKNOWN for "1 l" and "6 rashers" alike, while the
 * text has carried the unit all along. Scaling the number inside the text and leaving every other
 * word untouched keeps both correct without teaching this module every unit there is.
 *
 * Pure, and tested: it decides the amounts that end up on the shopping list.
 */

// Scraped recipes are full of these, and "½ tbsp" has to scale like any other half.
const VULGAR: Record<string, number> = {
    '½': 1 / 2, '⅓': 1 / 3, '⅔': 2 / 3, '¼': 1 / 4, '¾': 3 / 4,
    '⅕': 1 / 5, '⅖': 2 / 5, '⅗': 3 / 5, '⅘': 4 / 5,
    '⅙': 1 / 6, '⅚': 5 / 6, '⅐': 1 / 7, '⅛': 1 / 8, '⅜': 3 / 8, '⅝': 5 / 8, '⅞': 7 / 8,
};

const VULGAR_CLASS = `[${Object.keys(VULGAR).join('')}]`;

// One quantity, longest form first so "1 1/2" is not read as a bare "1" and "1½" not as "1".
const TOKEN = [
    `\\d+\\s*${VULGAR_CLASS}`,      // 1½
    VULGAR_CLASS,                    // ½
    '\\d+\\s+\\d+\\s*/\\s*\\d+',     // 1 1/2
    '\\d+\\s*/\\s*\\d+',             // 1/2
    '\\d+(?:\\.\\d+)?',              // 300, 1.5
].join('|');

const FIRST_TOKEN = new RegExp(TOKEN);
const TOKEN_AT_START = new RegExp(`^(?:${TOKEN})`);
// "2-3 peppers" is a range, and scaling only its first half would read "4-3".
const RANGE_GAP = /^\s*[-–—]\s*/;

// Only grams are rounded, and only where the unit stands as its own word: "1 kg" must not be read as
// grams, and neither must the "g" of "garlic".
const GRAMS = /^\s*(g|gs|gram|grams)\b/i;

function parse(token: string): number {
    const vulgar = token.match(new RegExp(VULGAR_CLASS));
    if (vulgar !== null) {
        const whole = token.slice(0, vulgar.index).trim();
        return (whole === '' ? 0 : Number(whole)) + VULGAR[vulgar[0]];
    }

    const mixed = /^(\d+)\s+(\d+)\s*\/\s*(\d+)$/.exec(token);
    if (mixed !== null) {
        return Number(mixed[1]) + Number(mixed[2]) / Number(mixed[3]);
    }

    const fraction = /^(\d+)\s*\/\s*(\d+)$/.exec(token);
    if (fraction !== null) {
        return Number(fraction[1]) / Number(fraction[2]);
    }

    return Number(token);
}

/** Trailing zeroes trimmed, and never more precision than a shopping list can use. */
function format(value: number): string {
    return String(Number(value.toFixed(2)));
}

/**
 * Whole items are deliberately not rounded: half an onion is a real instruction, and rounding it up
 * to one or two silently changes the recipe. Weights are, because nobody weighs out 333 g.
 */
function round(value: number, isGrams: boolean): number {
    if (!isGrams) {
        return value;
    }
    // Never round a real amount away to nothing.
    return Math.max(5, Math.round(value / 5) * 5);
}

/**
 * The text with its leading quantity multiplied by `factor`, or null when there is no number to
 * scale - "knob of butter" and "freshly grated parmesan" have nothing this can do for them, and are
 * better left in the recipe's own words than guessed at.
 */
export function scaleQuantityText(text: string | null, factor: number): string | null {
    if (text === null || text.trim().length === 0) {
        return null;
    }

    const first = FIRST_TOKEN.exec(text);
    if (first === null) {
        return null;
    }

    const value = parse(first[0]);
    if (!Number.isFinite(value) || value <= 0) {
        return null;
    }

    const before = text.slice(0, first.index);
    let after = text.slice(first.index + first[0].length);
    let scaled = '';

    // A range scales at both ends, so "2-3 peppers" doubles to "4-6 peppers".
    const gap = RANGE_GAP.exec(after);
    const upper = gap === null ? null : TOKEN_AT_START.exec(after.slice(gap[0].length));

    if (gap !== null && upper !== null) {
        const rest = after.slice(gap[0].length + upper[0].length);
        const isGrams = GRAMS.test(rest);
        scaled = format(round(value * factor, isGrams))
            + gap[0]
            + format(round(parse(upper[0]) * factor, isGrams));
        after = rest;
    } else {
        scaled = format(round(value * factor, GRAMS.test(after)));
    }

    return before + scaled + after;
}

/** True when scaling would change anything at all, so callers can skip the work entirely. */
export function isScaled(factor: number): boolean {
    return Number.isFinite(factor) && factor > 0 && Math.abs(factor - 1) > 1e-9;
}
