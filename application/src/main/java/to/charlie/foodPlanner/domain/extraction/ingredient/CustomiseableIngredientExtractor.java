package to.charlie.foodPlanner.domain.extraction.ingredient;

import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CustomiseableIngredientExtractor extends BaseIngredientExtractor {

  private final String recipeHeader;

  private final String recipeElement;

  public CustomiseableIngredientExtractor(String recipeHeader, Set<String> recipeElement) {
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
