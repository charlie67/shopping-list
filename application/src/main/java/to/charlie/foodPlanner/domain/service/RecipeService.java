package to.charlie.foodPlanner.domain.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.ExtractorFactory;
import to.charlie.foodPlanner.domain.extraction.ExtractorHolder;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedReceipeDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeStep;
import to.charlie.foodPlanner.domain.model.mapping.ExtractedIngredientMapper;
import to.charlie.foodPlanner.domain.model.mapping.ExtractedRecipeStepsMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

  public ExtractedReceipeDto extractRecipeFromUrl(String url) throws IOException {
    Document document;

    try {
      document = Jsoup.connect(url).get();
    } catch (IOException e) {
      log.info("Error getting page for recipe extraction", e);
      throw e;
    }

    String title = document.title();
    ExtractorHolder extractorHolder = ExtractorFactory.getExtractor(url);

    List<ExtractedIngredient> ingredients = extractorHolder.extractIngredients(document);
    List<ExtractedRecipeStep> recipeSteps = extractorHolder.extractRecipeSteps(document);

    return ExtractedReceipeDto.builder()
        .recipeName(title)
        .url(url)
        .steps(ExtractedRecipeStepsMapper.map(recipeSteps))
        .ingredients(ExtractedIngredientMapper.map(ingredients))
        .build();
  }
}
