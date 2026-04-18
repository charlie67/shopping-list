package to.charlie.foodPlanner.domain.extraction.manual;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Service
public class ManualExtractor implements RecipeExtractor {

  @Override
  public ExtractedRecipe extract(final Document document, final String url) {
    final String title = extractRecipeName(document);
    final String servings = extractRecipeYield(document);
    final String ingredients = extractIngredients(document);

    return ExtractedRecipe.builder()
        .name(title)
        .recipeYield(servings)
        .url(document.location())
        .build();
  }

  private String extractRecipeName(final Document document) {
    final Element title = document.selectFirst("meta[property=og:title]");
    if (title != null) {
      return title.attr("content");
    }
    return document.title();
  }

  private String extractRecipeYield(final Document document) {
    final Element makes = document.selectFirst(":containsOwn(makes:)");
    if (makes != null) {
      return makes.text().toLowerCase().replace("makes:", "").trim();
    }

    final Element serves = document.selectFirst(":containsOwn(serves:)");
    if (serves != null) {
      return serves.text().toLowerCase().replace("serves:", "").trim();
    }

    final Element servings = document.selectFirst(":containsOwn(servings:)");
    if (servings != null) {
      return servings.text().toLowerCase().replace("servings:", "").trim();
    }

    final Element yield = document.selectFirst(":containsOwn(yield:)");
    if (yield != null) {
      return yield.text().toLowerCase().replace("yield:", "").trim();
    }

    return "";
  }

  private void checkElementIsInstruction(final Element node) {
    final String text = node.text();

    final boolean moreThan100Characters = text.length() >= 100;
    final boolean startWithCapitalLetter = Character.isUpperCase(text.charAt(0));
    final boolean containAnyCapitalLetters = text.chars().anyMatch(Character::isUpperCase);
    final boolean endsWithPunctuation =
        text.endsWith(".") || text.endsWith("!") || text.endsWith("?") || text.endsWith(":")
            || text.endsWith(";");
  }

  private String extractIngredients(final Document document) {
    final Element body = document.selectFirst("body");
    for (final Element child : body.children()) {
      child.text();
    }
    return "";
  }

  private void checkElementIsIngredient(final Element node) {
    final int maxExpectedLength = 30;

    final String text = node.text();

  }
}
