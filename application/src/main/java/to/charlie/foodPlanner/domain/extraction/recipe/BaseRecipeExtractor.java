package to.charlie.foodPlanner.domain.extraction.recipe;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import to.charlie.foodPlanner.domain.extraction.ExtractorUtils;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedItem;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeStep;

public class BaseRecipeExtractor {

  public List<ExtractedRecipeStep> extractRecipeSteps(Document document) {
    List<ExtractedItem> recipeSteps = new ArrayList<>();

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

    ExtractorUtils.removeDuplicates(recipeSteps);

    return (List<ExtractedRecipeStep>) (List<?>) recipeSteps;
  }

  Elements getRecipeElements(Element recipeHeader) {
    Elements lis = recipeHeader.siblingElements().select("li, ul, p");
    lis.addAll(recipeHeader.select("li, ul, p, h, strong"));
    return lis;
  }

  Elements getHeader(Document document) {
    return document.select(
        "*:containsOwn(Method), *:containsOwn(Steps), *:containsOwn(Directions)");
  }
}
