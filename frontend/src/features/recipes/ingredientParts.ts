import {RecipeIngredient} from '@/common/types/recipe';
import {isScaled, scaleQuantityText} from './scaleIngredient';

const isBlank = (value: string | null | undefined) => !value || value.trim().length === 0;

/**
 * The shopping-list version of an ingredient: its quantity and name, with the recipe's wording
 * dropped — "400g can black beans drained" becomes "400 g black beans". Returns null when there is
 * nothing to shorten to (no ingredient name, or the result would just repeat the original), in which
 * case only the original text is worth offering.
 */
export function shortenIngredient(ingredient: RecipeIngredient, factor = 1): string | null {
    if (isBlank(ingredient.ingredientName)) {
        return null;
    }

    // A batch cooked for a different number of people carries different amounts onto the list. When
    // the quantity has no number to scale ("knob of butter") the recipe's own wording stands.
    const scaling = isScaled(factor);
    const quantity = scaling
        ? scaleQuantityText(ingredient.quantityText, factor) ?? ingredient.quantityText
        : ingredient.quantityText;

    const shortened = [quantity, ingredient.ingredientName]
        .filter((part) => !isBlank(part))
        .map((part) => part!.trim())
        .join(' ');

    // Unscaled, a shortening that only repeats the original is not worth offering. Scaled, it says
    // something the original does not, so it is always worth showing.
    if (scaling) {
        return shortened;
    }
    return shortened === ingredient.fullText.trim() ? null : shortened;
}
