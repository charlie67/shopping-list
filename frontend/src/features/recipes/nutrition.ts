import {ExtractedRecipeDto} from '@/common/types/recipe';

export interface NutritionFact {
    label: string;
    value: string;
}

// Ordered the way recipe sites tend to list them, most useful first.
const NUTRITION_FIELDS = [
    ['calories', 'Calories'],
    ['proteinContent', 'Protein'],
    ['carbohydrateContent', 'Carbs'],
    ['fatContent', 'Fat'],
    ['saturatedFatContent', 'Saturates'],
    ['sugarContent', 'Sugar'],
    ['fiberContent', 'Fibre'],
    ['sodiumContent', 'Sodium'],
] as const satisfies readonly (readonly [keyof ExtractedRecipeDto, string])[];

const AMOUNT = /-?\d+(?:[.,]\d+)?/;

// Longer names first, so "milligram" is not read as "gram".
const UNITS: readonly (readonly [RegExp, string])[] = [
    [/kcal|calorie/i, 'kcal'],
    [/microgram|\bmcg\b|µg/i, 'µg'],
    [/milligram|\bmg\b/i, 'mg'],
    [/kilogram|\bkg\b/i, 'kg'],
    [/millilit|\bml\b/i, 'ml'],
    [/gram|\bg\b/i, 'g'],
];

/**
 * Shortens a nutrition value to something that fits in a tile. Sites word these however they like —
 * "454 calories", "23 grams fat", "1.38 milligram of sodium", "1.8 g" — so the amount and the unit
 * are pulled out and the site's prose is dropped. Anything that does not look like an amount with a
 * recognisable unit is left exactly as it came, rather than mangled.
 */
export function formatNutritionValue(raw: string): string {
    const text = raw.trim();
    const amount = text.match(AMOUNT)?.[0];
    if (!amount) {
        return text;
    }

    const unit = UNITS.find(([pattern]) => pattern.test(text))?.[1];
    if (!unit) {
        return text;
    }

    return unit === 'kcal' ? `${amount} kcal` : `${amount}${unit}`;
}

/**
 * The nutrition values a recipe actually has, ready to render. Extraction fills in whatever the
 * source published, so anything missing is left out rather than shown as a blank.
 */
export function nutritionFacts(recipe: ExtractedRecipeDto): NutritionFact[] {
    return NUTRITION_FIELDS.flatMap(([field, label]) => {
        const raw = recipe[field];
        if (typeof raw !== 'string' || raw.trim().length === 0) {
            return [];
        }
        return [{label, value: formatNutritionValue(raw)}];
    });
}
