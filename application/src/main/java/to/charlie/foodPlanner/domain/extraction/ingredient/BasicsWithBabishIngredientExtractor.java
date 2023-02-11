package to.charlie.foodPlanner.domain.extraction.ingredient;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BasicsWithBabishIngredientExtractor extends BaseIngredientExtractor {

  @Override
  Elements getIngredientElements(Element ingredientHeader) {
    return ingredientHeader.siblingElements().select("p");
  }
}
