import {describe, expect, it} from 'vitest';
import {isScaled, scaleQuantityText} from './scaleIngredient';

describe('scaleQuantityText', () => {
    it('scales a whole number and keeps the unit words', () => {
        expect(scaleQuantityText('2 tbsps', 1.5)).toBe('3 tbsps');
        expect(scaleQuantityText('6 rashers', 1.5)).toBe('9 rashers');
        // The parsed unit says UNKNOWN for litres, so the text is the only thing that knows.
        expect(scaleQuantityText('1 l', 2)).toBe('2 l');
    });

    // Half an onion is a real instruction; rounding it silently changes the recipe.
    it('leaves whole items on a fraction rather than rounding them', () => {
        expect(scaleQuantityText('1', 1.5)).toBe('1.5');
        expect(scaleQuantityText('1 onion', 1.5)).toBe('1.5 onion');
        expect(scaleQuantityText('3 cloves', 0.5)).toBe('1.5 cloves');
    });

    it('rounds grams to the nearest 5', () => {
        expect(scaleQuantityText('300 g', 1.5)).toBe('450 g');
        expect(scaleQuantityText('500 g', 2 / 3)).toBe('335 g');
        expect(scaleQuantityText('100g', 1.33)).toBe('135g');
        expect(scaleQuantityText('250 grams', 0.5)).toBe('125 grams');
    });

    it('never rounds a real weight away to nothing', () => {
        expect(scaleQuantityText('10 g', 0.1)).toBe('5 g');
    });

    it('does not treat kg, or a word starting with g, as grams', () => {
        expect(scaleQuantityText('1 kg', 1.5)).toBe('1.5 kg');
        expect(scaleQuantityText('2 garlic cloves', 1.5)).toBe('3 garlic cloves');
    });

    it('reads fractions and mixed fractions', () => {
        expect(scaleQuantityText('1/2 tsp', 2)).toBe('1 tsp');
        expect(scaleQuantityText('1 1/2 tbsp', 2)).toBe('3 tbsp');
        expect(scaleQuantityText('1/4 cup', 3)).toBe('0.75 cup');
    });

    it('gives up on a quantity with no number in it', () => {
        expect(scaleQuantityText('knob', 1.5)).toBeNull();
        expect(scaleQuantityText('to taste', 2)).toBeNull();
        expect(scaleQuantityText(null, 2)).toBeNull();
        expect(scaleQuantityText('   ', 2)).toBeNull();
    });

    it('keeps at most two decimals', () => {
        expect(scaleQuantityText('1 onion', 1 / 3)).toBe('0.33 onion');
        expect(scaleQuantityText('2 tbsp', 1 / 3)).toBe('0.67 tbsp');
    });

    it('scales the first number and leaves the rest of the wording alone', () => {
        expect(scaleQuantityText('400g can black beans drained', 1.5))
            .toBe('600g can black beans drained');
    });

    it('is a no-op at a factor of one', () => {
        expect(scaleQuantityText('300 g', 1)).toBe('300 g');
        expect(scaleQuantityText('1 onion', 1)).toBe('1 onion');
    });
});

// The quantity strings of a real scraped recipe, cooked for 6 instead of the 4 it was written for.
// Invented examples would not have caught that extraction leaves the unit UNKNOWN for "1 l" and
// "6 rashers", so the unit only ever existed in this text.
describe('a real recipe scaled from 4 to 6', () => {
    const forSix = (text: string | null) => scaleQuantityText(text, 6 / 4);

    it('scales every quantity it can read', () => {
        expect(forSix('1')).toBe('1.5');            // 1 onion
        expect(forSix('2 tbsps')).toBe('3 tbsps');  // olive oil
        expect(forSix('6 rashers')).toBe('9 rashers');
        expect(forSix('300 g')).toBe('450 g');      // risotto rice
        expect(forSix('1 l')).toBe('1.5 l');        // hot vegetable stock
        expect(forSix('100 g')).toBe('150 g');      // frozen peas
    });

    it('leaves the ones with no number in the recipe’s own words', () => {
        expect(forSix('knob')).toBeNull();          // knob of butter
        expect(forSix(null)).toBeNull();            // freshly grated parmesan, to serve
    });
});

// A second real recipe, which is where both of these turned up. Neither was reachable from invented
// examples: scraped text is full of "½" and of ranges, and scaling only the first half of a range
// produced "4-3".
describe('a real recipe scaled from 2 to 4', () => {
    const forFour = (text: string) => scaleQuantityText(text, 4 / 2);

    it('doubles the weights', () => {
        expect(forFour('200g penne')).toBe('400g penne');
        expect(forFour('100g chorizo skin removed')).toBe('200g chorizo skin removed');
        expect(forFour('400g can cherry tomatoes')).toBe('800g can cherry tomatoes');
    });

    it('reads the vulgar fractions scraped recipes are written with', () => {
        expect(forFour('½ tbsp olive oil')).toBe('1 tbsp olive oil');
        expect(forFour('½ small pack basil leaves')).toBe('1 small pack basil leaves');
    });

    it('scales both ends of a range', () => {
        expect(forFour('2-3 guindilla pickled chilli peppers')).toBe('4-6 guindilla pickled chilli peppers');
    });

    it('leaves an ingredient with no quantity alone', () => {
        expect(forFour('parmesan grated, to serve')).toBeNull();
    });
});

describe('vulgar fractions and ranges', () => {
    it('handles a whole number with a fraction stuck to it', () => {
        expect(scaleQuantityText('1½ tbsp', 2)).toBe('3 tbsp');
        expect(scaleQuantityText('2¼ cups', 2)).toBe('4.5 cups');
    });

    it('handles the other common fractions', () => {
        expect(scaleQuantityText('¼ tsp', 4)).toBe('1 tsp');
        expect(scaleQuantityText('⅔ cup', 3)).toBe('2 cup');
        expect(scaleQuantityText('¾ tsp', 4)).toBe('3 tsp');
    });

    it('rounds both ends of a gram range', () => {
        expect(scaleQuantityText('100-150 g mince', 1.5)).toBe('150-225 g mince');
    });

    it('reads an en dash as a range too', () => {
        expect(scaleQuantityText('2–3 peppers', 2)).toBe('4–6 peppers');
    });

    it('is not fooled by a hyphen that is not a range', () => {
        expect(scaleQuantityText('2 slow-cooked onions', 2)).toBe('4 slow-cooked onions');
    });
});

describe('isScaled', () => {
    it('is false for a factor of one, and for nonsense', () => {
        expect(isScaled(1)).toBe(false);
        expect(isScaled(Number.NaN)).toBe(false);
        expect(isScaled(0)).toBe(false);
        expect(isScaled(-2)).toBe(false);
    });

    it('is true for a real change in either direction', () => {
        expect(isScaled(1.5)).toBe(true);
        expect(isScaled(0.5)).toBe(true);
    });
});
