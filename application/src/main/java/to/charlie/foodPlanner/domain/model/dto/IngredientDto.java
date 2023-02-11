package to.charlie.foodPlanner.domain.model.dto;

import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

public record IngredientDto(String ingredientName, double quantity, MeasurementUnit unit) {}
