package to.charlie.foodPlanner.domain.extraction.ingredient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BbcGoodFoodIngredientExtractor extends BaseIngredientExtractor {

  @Override
  Elements getIngredientHeaders(Document document) {
    return document.select("h2:contains(Ingredients)");
  }

  @Override
  Elements getIngredientElements(Element ingredientHeader) {
    return ingredientHeader.siblingElements().select("li");
  }
}
