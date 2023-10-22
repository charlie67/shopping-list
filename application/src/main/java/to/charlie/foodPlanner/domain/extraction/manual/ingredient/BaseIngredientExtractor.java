package to.charlie.foodPlanner.domain.extraction.manual.ingredient;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import to.charlie.foodPlanner.domain.extraction.ExtractorUtils;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedIngredient;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedItem;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;
import to.charlie.foodPlanner.domain.extraction.QuantityExtractor;

public class BaseIngredientExtractor {

  private final QuantityExtractor quantityExtractor;

  public BaseIngredientExtractor() {
    this.quantityExtractor = new QuantityExtractor();
  }

  public List<ExtractedIngredient> extractIngredients(Document document) {
    List<ExtractedItem> ingredients = new ArrayList<>();

    // Find the element with the "ingredients" header
    Elements ingredientsHeaders = getIngredientHeaders(document);
    ingredientsHeaders.forEach(header ->  getIngredientElements(header).forEach(li -> extractIngredientFromElement(li, ingredients)));

    if (ingredients.isEmpty()) {
      extractOtherCommonPhrases(document).forEach(li -> extractIngredientFromElement(li, ingredients));
    }

    ExtractorUtils.removeIngredientDuplicates(ingredients);
    return (List<ExtractedIngredient>) (List<?>) ingredients;
  }

  Elements getIngredientElements(Element ingredientHeader) {
    Elements lis = ingredientHeader.siblingElements().select("li");
    lis.addAll(ingredientHeader.select("p"));
    return lis;
  }

  Elements getIngredientHeaders(Document document) {
    return document.select("h2:contains(Ingredients), h3:contains(Ingredients), h4:contains(Ingredients)");
  }

  private void extractIngredientFromElement(Element elem, List<ExtractedItem> ingredients) {
    if (StringUtils.isEmpty(elem.text())) {
      return;
    }

    String originalText = elem.text();
    String tagName = elem.tagName();
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

  private Elements extractOtherCommonPhrases(Document document) {
    Elements liIngredientList = document.select("ul.ingredient-list").select("li");
    if (!liIngredientList.isEmpty()) {
      return liIngredientList;
    }

    return document.select("p:containsOwn(Ingredients)").select("p");
  }
}
