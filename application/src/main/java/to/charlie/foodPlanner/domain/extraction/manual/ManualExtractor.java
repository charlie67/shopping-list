package to.charlie.foodPlanner.domain.extraction.manual;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Service
public class ManualExtractor implements RecipeExtractor {

  @Override
  public ExtractedRecipe extract(Document document) {
    String title = extractRecipeName(document);
    String servings = extractRecipeYield(document);
    String ingredients = extractIngredients(document);

    return ExtractedRecipe.builder()
        .name(title)
        .recipeYield(servings)
        .url(document.location())
        .build();
  }

  private String extractRecipeName(Document document) {
    Element title = document.selectFirst("meta[property=og:title]");
    if (title!= null) {
      return title.attr("content");
    }
    return document.title();
  }

  private String extractRecipeYield(Document document) {
    Element makes = document.selectFirst(":containsOwn(makes:)");
    if (makes != null) {
      return makes.text().toLowerCase().replace("makes:", "").trim();
    }

    Element serves = document.selectFirst(":containsOwn(serves:)");
    if (serves != null) {
      return serves.text().toLowerCase().replace("serves:", "").trim();
    }

    Element servings = document.selectFirst(":containsOwn(servings:)");
    if (servings != null) {
      return servings.text().toLowerCase().replace("servings:", "").trim();
    }

    Element yield = document.selectFirst(":containsOwn(yield:)");
    if (yield != null) {
      return yield.text().toLowerCase().replace("yield:", "").trim();
    }

    return "";
  }

  private void checkElementIsInstruction(Element node) {
    String text = node.text();

    boolean moreThan100Characters = text.length() >= 100;
    boolean startWithCapitalLetter = Character.isUpperCase(text.charAt(0));
    boolean containAnyCapitalLetters = text.chars().anyMatch(Character::isUpperCase);
    boolean endsWithPunctuation = text.endsWith(".") || text.endsWith("!") || text.endsWith("?") || text.endsWith(":") || text.endsWith(";");
  }

  private String extractIngredients(Document document) {
    Element body = document.selectFirst("body");
    for (Element child : body.children()) {
      child.text();
    }
    return "";
  }

  private void checkElementIsIngredient(Element node) {
    int maxExpectedLength = 30;

    String text = node.text();

  }
}
