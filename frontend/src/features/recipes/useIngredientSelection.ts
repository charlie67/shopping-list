import {useMemo, useState} from 'react';
import type {RecipeIngredient} from '@/common/types/recipe';
import {shortenIngredient} from './ingredientParts';
import {isScaled, scaleQuantityText} from './scaleIngredient';

export interface IngredientSelection {
    selected: Set<number>;
    useOriginal: Set<number>;
    shortTexts: (string | null)[];
    allSelected: boolean;
    /** The wording that would go on the shopping list for this ingredient. */
    textFor: (index: number) => string;
    toggle: (index: number) => void;
    toggleAll: () => void;
    setOriginal: (index: number, original: boolean) => void;
    /** The chosen wording for every ticked ingredient, ready to post. */
    titles: () => string[];
}

/**
 * Which of a recipe's ingredients are going on the shopping list, and in whose words. Shared by the
 * ingredients-only picker and the plan sheet so the two cannot drift apart.
 *
 * `factor` scales the quantities for a batch cooked for a different number of people. It changes only
 * what is written on the shopping list - the stored recipe keeps its own amounts.
 */
export function useIngredientSelection(
    ingredients: RecipeIngredient[],
    factor = 1,
): IngredientSelection {
    const [selected, setSelected] = useState<Set<number>>(
        () => new Set(ingredients.map((_, i) => i)),
    );
    // The shortened "quantity + ingredient" text per ingredient, or null when there is nothing to
    // shorten. Recomputed when the batch is scaled, since that is what moves the quantities.
    const shortTexts = useMemo(
        () => ingredients.map((ingredient) => shortenIngredient(ingredient, factor)),
        [ingredients, factor],
    );

    // The recipe's own wording, with its first number scaled. Offered instead of the shortened form
    // when the user prefers it, and it has to move with the batch too or the two would disagree.
    const originalTexts = useMemo(
        () => ingredients.map((ingredient) => (isScaled(factor)
            ? scaleQuantityText(ingredient.fullText, factor) ?? ingredient.fullText
            : ingredient.fullText)),
        [ingredients, factor],
    );
    // Indexes showing their original wording instead of the shortened version. Shortening is the
    // default, so this starts empty.
    const [useOriginal, setUseOriginal] = useState<Set<number>>(() => new Set());

    const textFor = (index: number) =>
        (!useOriginal.has(index) && shortTexts[index]) || originalTexts[index];

    const toggle = (index: number) => {
        setSelected((prev) => {
            const next = new Set(prev);
            if (next.has(index)) {
                next.delete(index);
            } else {
                next.add(index);
            }
            return next;
        });
    };

    const allSelected = selected.size === ingredients.length;

    const toggleAll = () => {
        setSelected(allSelected ? new Set() : new Set(ingredients.map((_, i) => i)));
    };

    const setOriginal = (index: number, original: boolean) => {
        setUseOriginal((prev) => {
            const next = new Set(prev);
            if (original) {
                next.add(index);
            } else {
                next.delete(index);
            }
            return next;
        });
    };

    const titles = () => ingredients
        .map((_, i) => (selected.has(i) ? textFor(i) : null))
        .filter((title): title is string => title !== null);

    return {selected, useOriginal, shortTexts, allSelected, textFor, toggle, toggleAll, setOriginal, titles};
}
