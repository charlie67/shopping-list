export enum IngredientUnit {
    GRAMS = 'GRAMS',
    OUNCES = 'OUNCES',
    TABLESPOONS = 'TABLESPOONS',
    TEASPOONS = 'TEASPOONS',
    CUPS = 'CUPS',
    UNKNOWN = 'UNKNOWN',
}

// The wire values the backend's ExtractionMethod enum serialises to, not its constant names.
export type ExtractionMethod = 'microdata' | 'JSON-LD' | 'JustTheRecipe';

export const JUST_THE_RECIPE: ExtractionMethod = 'JustTheRecipe';

export interface RecipeInstruction {
    text: string;
    type: string;
    stepCount: number;
    possibleDuplicate: boolean;
}

export interface RecipeIngredient {
    ingredientName: string;
    quantity: number;
    // Extraction leaves the unit unset for ingredients with no measurable quantity.
    unit: IngredientUnit | null;
    size: string | null;
    preparation: string | null;
    purpose: string | null;
    comment: string | null;
    quantityText: string | null;
    fullText: string;
    possibleDuplicate: boolean;
}

export interface ExtractedRecipeDto {
    id: string;
    url: string;
    name: string;
    imageUrl: string;
    description: string;
    dateModified: string | null;
    datePublished: string | null;
    keywords: string | null;
    cookTime: string | null;
    prepTime: string | null;
    totalTime: string | null;
    recipeCategory: string | null;
    recipeYield: string | null;
    calories: string | null;
    fatContent: string | null;
    saturatedFatContent: string | null;
    carbohydrateContent: string | null;
    sugarContent: string | null;
    fiberContent: string | null;
    proteinContent: string | null;
    sodiumContent: string | null;
    extractionMethod: ExtractionMethod | null;
    instructions: RecipeInstruction[];
    ingredients: RecipeIngredient[];
}
