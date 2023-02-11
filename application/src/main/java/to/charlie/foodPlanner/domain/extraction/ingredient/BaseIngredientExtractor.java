package to.charlie.foodPlanner.domain.extraction.ingredient;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import to.charlie.foodPlanner.domain.extraction.ExtractorUtils;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedItem;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.QuantityExtractor;

public class BaseIngredientExtractor {

  private final QuantityExtractor quantityExtractor;

  public BaseIngredientExtractor() {
    this.quantityExtractor = new QuantityExtractor();
  }

  public List<ExtractedIngredient> extractIngredients(Document document) {
    List<ExtractedItem> ingredients = new ArrayList<>();

    // Find the element with the "ingredients" header
    Elements ingredientsHeaders = getIngredientHeaders(document);

    if (ingredientsHeaders.isEmpty()) {
      return List.of();
    }

    for (Element ingredientHeader : ingredientsHeaders) {
      Elements lis = getIngredientElements(ingredientHeader);

      for (Element li : lis) {

        if (StringUtils.isEmpty(li.text())) {
          continue;
        }

        String originalText = li.text();
        String tagName = li.tagName();
        String modifiedText =
            String.join(
                "/", Normalizer.normalize(originalText, Normalizer.Form.NFKD).split("\u2044"));

        IngredientMeasurement ingredientMeasurement;
        String ingredientName;

        if (!modifiedText.equals(originalText)) {
          ingredientMeasurement = quantityExtractor.extractQuantityFromIngredient(modifiedText);
          ingredientName =
              modifiedText.replace(ingredientMeasurement.quantityUnitText(), "").trim();
        } else {
          ingredientMeasurement = quantityExtractor.extractQuantityFromIngredient(originalText);
          ingredientName =
              originalText.replace(ingredientMeasurement.quantityUnitText(), "").trim();
        }

        ingredients.add(
            new ExtractedIngredient(
                originalText, modifiedText, tagName, ingredientMeasurement, ingredientName, false));
      }
    }

    ExtractorUtils.removeDuplicates(ingredients);
    return (List<ExtractedIngredient>) (List<?>) ingredients;
  }

  Elements getIngredientElements(Element ingredientHeader) {
    Elements lis = ingredientHeader.siblingElements().select("li, ul, p");
    lis.addAll(ingredientHeader.select("p"));
    return lis;
  }

  Elements getIngredientHeaders(Document document) {
    return document.select("h2:contains(Ingredients)");
  }
}
