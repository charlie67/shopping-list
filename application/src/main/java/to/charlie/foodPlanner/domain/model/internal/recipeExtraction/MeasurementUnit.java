package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import java.util.HashMap;
import java.util.Map;

public enum MeasurementUnit {
  GRAMS(1, true, "G", "GRAM"),
  OUNCES(28.3495, true, "OZ", "OUNCE"),
  TABLESPOONS(1, false, "TBSP", "TABLESPOON"),
  TEASPOONS(1, false, "TSP", "TEASPOON"),
  CUPS(1, false, "CUP"),

  UNKNOWN(1, false);

  private static final Map<String, MeasurementUnit> creationMap = new HashMap<>();

  static {
    createNameMap();
  }

  private final boolean canConvert;
  private final double conversionAmount;
  private final String[] otherNames;

  MeasurementUnit(final double conversionAmount, final boolean canConvert, final String... otherNames) {
    this.conversionAmount = conversionAmount;
    this.canConvert = canConvert;
    this.otherNames = otherNames;
  }

  public static void createNameMap() {
    for (final MeasurementUnit value : values()) {
      creationMap.put(value.name(), value);

      for (final String otherName : value.otherNames) {
        creationMap.put(otherName, value);
      }
    }
  }

  public static MeasurementUnit convert(final String input) {
    final String inputUpperTrimmed = input.toUpperCase().trim();

    if (creationMap.containsKey(inputUpperTrimmed)) {
      return creationMap.get(inputUpperTrimmed);
    }

    return UNKNOWN;
  }
}
