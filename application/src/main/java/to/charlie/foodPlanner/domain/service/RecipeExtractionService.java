package to.charlie.foodPlanner.domain.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Slf4j
@Service
public class RecipeExtractionService {

  private final List<RecipeExtractor> orderedRecipeExtractors;

  public RecipeExtractionService( @Qualifier("orderedRecipeExtractors") List<RecipeExtractor> orderedRecipeExtractors) {
    this.orderedRecipeExtractors = orderedRecipeExtractors;
  }

  public ExtractedRecipe extractRecipe(final Document document, final String url) {
    for (RecipeExtractor recipeExtractor : orderedRecipeExtractors) {
      try {
        return recipeExtractor.extract(document);
      } catch (final IllegalArgumentException e) {
        log.info("Unable to extract recipe using {} from {}", recipeExtractor.getClass().getSimpleName(), url);
      }
    }

    throw new IllegalArgumentException("Exhausted all recipe extractors");
  }
}


