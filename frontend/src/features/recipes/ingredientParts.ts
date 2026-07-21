import {RecipeIngredient} from '@/common/types/recipe';

const isBlank = (value: string | null | undefined) => !value || value.trim().length === 0;

/**
 * The shopping-list version of an ingredient: its quantity and name, with the recipe's wording
 * dropped — "400g can black beans drained" becomes "400 g black beans". Returns null when there is
 * nothing to shorten to (no ingredient name, or the result would just repeat the original), in which
 * case only the original text is worth offering.
 */
export function shortenIngredient(ingredient: RecipeIngredient): string | null {
    if (isBlank(ingredient.ingredientName)) {
        return null;
    }

    const shortened = [ingredient.quantityText, ingredient.ingredientName]
        .filter((part) => !isBlank(part))
        .map((part) => part!.trim())
        .join(' ');

    return shortened === ingredient.fullText.trim() ? null : shortened;
}
