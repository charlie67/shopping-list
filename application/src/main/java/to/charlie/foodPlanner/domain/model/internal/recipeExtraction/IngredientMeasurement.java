package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

public record IngredientMeasurement(
    double quantity, String unitText, String quantityUnitText, MeasurementUnit unit) {

  public static IngredientMeasurement createBlankItem() {
    return new IngredientMeasurement(0, "", "", MeasurementUnit.UNKNOWN);
  }
}
