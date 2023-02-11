package to.charlie.foodPlanner.domain.extraction.recipe;

import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CustomiseableRecipeExtractor extends BaseRecipeExtractor {

  private final String recipeHeader;

  private final String recipeElement;

  public CustomiseableRecipeExtractor(String recipeHeader, Set<String> recipeElement) {
    this.recipeHeader = recipeHeader;
    this.recipeElement = String.join(", ", recipeElement);
  }

  Elements getRecipeElements(Element recipeHeader) {
    return recipeHeader.siblingElements().select(recipeElement);
  }

  Elements getHeader(Document document) {
    return document.select("*:matchesOwn(" + recipeHeader + ")");
  }
}
