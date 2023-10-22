package to.charlie.foodPlanner.domain.extraction.manual.recipe;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import to.charlie.foodPlanner.domain.extraction.ExtractorUtils;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedItem;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedRecipeStep;

public class BaseRecipeExtractor {

  public List<ExtractedRecipeStep> extractRecipeSteps(Document document) {
    List<ExtractedRecipeStep> recipeSteps = new ArrayList<>();

    Elements recipeHeaders = getHeader(document);

    if (recipeHeaders.isEmpty()) {
      return List.of();
    }

    for (Element recipeHeader : recipeHeaders) {
      Elements lis = getRecipeElements(recipeHeader);

      for (Element li : lis) {
        if (StringUtils.isEmpty(li.text())) {
          continue;
        }

        String originalText = li.text();
        String tagName = li.tagName();

        recipeSteps.add(new ExtractedRecipeStep(originalText, tagName, false));
      }
    }

    ExtractorUtils.removeStepDuplicates( (List<ExtractedItem>) (List<?>) recipeSteps);

    return recipeSteps;
  }

  Elements getRecipeElements(Element recipeHeader) {
    Elements lis = recipeHeader.siblingElements().select("li, p");
    lis.addAll(recipeHeader.select("li, p, h, strong"));
    return lis;
  }

  Elements getHeader(Document document) {
    Elements possibleSections = document.select(
            "*:containsOwn(Method), *:containsOwn(Steps), *:containsOwn(Directions), *:containsOwn(Preparation), " +
                    "*:containsOwn(Instructions), *:containsOwn(Recipe)");

    List<String> ignoreTagNames = List.of("title", "meta", "script", "style", "head", "html", "body", "a", "p", "span");
    possibleSections.removeIf(possibleSection -> ignoreTagNames.contains(possibleSection.tagName()));

    return possibleSections;
  }
}
