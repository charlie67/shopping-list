export enum IngredientUnit {
    GRAMS = 'GRAMS',
    OUNCES = 'OUNCES',
    TABLESPOONS = 'TABLESPOONS',
    TEASPOONS = 'TEASPOONS',
    CUPS = 'CUPS',
    UNKNOWN = 'UNKNOWN',
}

export interface RecipeInstruction {
    text: string;
    type: string;
    stepCount: number;
    possibleDuplicate: boolean;
}

export interface RecipeIngredient {
    ingredientName: string;
    quantity: number;
    unit: IngredientUnit;
    fullText: string;
    possibleDuplicate: boolean;
}

export interface ExtractedRecipeDto {
    id: string;
    url: string;
    name: string;
    imageUrl: string;
    description: string;
    instructions: RecipeInstruction[];
    ingredients: RecipeIngredient[];
}
