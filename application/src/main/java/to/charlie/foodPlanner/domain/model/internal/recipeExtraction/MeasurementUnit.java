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

  private final boolean canConvert;
  private String[] otherNames;
  private final double conversionAmount;

  private static Map<String, MeasurementUnit> creationMap = new HashMap<>();

  MeasurementUnit(double conversionAmount, boolean canConvert, String... otherNames) {
    this.conversionAmount = conversionAmount;
    this.canConvert = canConvert;
    this.otherNames = otherNames;
  }

  static {
    createNameMap();
  }

  public static void createNameMap() {
    for (MeasurementUnit value : values()) {
      creationMap.put(value.name(), value);

      for (String otherName : value.otherNames) {
        creationMap.put(otherName, value);
      }
    }
  }

  public static MeasurementUnit convertInputToEnum(String input) {
    String inputUpperTrimmed = input.toUpperCase().trim();

    if (creationMap.containsKey(inputUpperTrimmed)) {
      return creationMap.get(inputUpperTrimmed);
    }

    return UNKNOWN;
  }
}
