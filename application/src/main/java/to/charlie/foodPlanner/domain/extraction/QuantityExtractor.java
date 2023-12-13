package to.charlie.foodPlanner.domain.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

@NoArgsConstructor
@Component
public class QuantityExtractor {

  public IngredientMeasurement extractQuantityFromIngredient(String ingredient) {
    String pattern =
        "^([\\d/\\s]+)((?:cups?|teaspoons?|tablespoons?|grams?|ounces?|drops?|oz|g|kg|tbsp|tsp)(?:es)?)\\b";
    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    Matcher m = r.matcher(ingredient);
    if (m.find()) {
      String quantityText = m.group(1).trim();
      double quantity = extractFractionQuantityAndTurnIntoDecimal(quantityText);
      String unitText = m.group(2).trim();
      String quantityUnitText = quantityText + " " + unitText;

      return new IngredientMeasurement(
          quantity, unitText, quantityUnitText, MeasurementUnit.convert(unitText));
    }

    return IngredientMeasurement.createBlankItem();
  }

  private double extractFractionQuantityAndTurnIntoDecimal(String quantityString) {
    if (quantityString.contains("/")) {

      String[] parts = quantityString.split("/");
      String firstHalf = parts[0];
      String secondHalf = parts[1];
      String[] firstHalfSplit = firstHalf.split(" ");
      String[] secondHalfSplit = secondHalf.split(" ");
      int numerator = Integer.parseInt(firstHalfSplit[firstHalfSplit.length - 1].trim());
      int denominator = Integer.parseInt(secondHalfSplit[0].trim());

      String fractionString = numerator + "/" + denominator;

      String wholeQuantityString = quantityString.replace(fractionString, "").trim();
      int wholeQuantity;
      if (wholeQuantityString.isEmpty()) {
        wholeQuantity = 0;
      } else {
        wholeQuantity = Integer.parseInt(wholeQuantityString);
      }

      return wholeQuantity + ((double) numerator / denominator);
    } else {
      return Integer.parseInt(quantityString.trim());
    }
  }
}
