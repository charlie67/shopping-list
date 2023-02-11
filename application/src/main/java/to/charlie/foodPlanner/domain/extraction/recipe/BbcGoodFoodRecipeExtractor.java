package to.charlie.foodPlanner.domain.extraction.recipe;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BbcGoodFoodRecipeExtractor extends BaseRecipeExtractor {

  @Override
  Elements getHeader(Document document) {
    return document.select("*:matchesOwn(Method)");
  }

  @Override
  Elements getRecipeElements(Element recipeHeader) {
    return recipeHeader.siblingElements().select("li");
  }
}
