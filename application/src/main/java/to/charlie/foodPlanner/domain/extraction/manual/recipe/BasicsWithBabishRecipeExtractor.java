package to.charlie.foodPlanner.domain.extraction.manual.recipe;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BasicsWithBabishRecipeExtractor extends BaseRecipeExtractor {

  @Override
  Elements getRecipeElements(Element recipeHeader) {
    Elements lis = recipeHeader.siblingElements().select("p");
    lis.addAll(recipeHeader.select("p"));
    return lis;
  }

  @Override
  Elements getHeader(Document document) {
    return document.select("h2:contains(Method)");
  }
}
