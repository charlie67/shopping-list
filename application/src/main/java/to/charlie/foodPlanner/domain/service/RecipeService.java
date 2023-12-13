package to.charlie.foodPlanner.domain.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.dal.dao.RecipeDao;
import to.charlie.foodPlanner.domain.model.converter.recipe.ExtractedRecipeToDtoConverter;
import to.charlie.foodPlanner.domain.model.dto.recipe.RecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

  public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:101.0) Gecko/20100101 Firefox/101.0";
  public static final String REFERRER = "https://www.google.com";

  private final RecipeDao recipeDao;
  private final ExtractedRecipeToDtoConverter extractedRecipeToDtoConverter;
  private final RecipeExtractionService recipeExtractionService;

  public ExtractedRecipeDto extractRecipeFromUrl(final String url)
      throws IOException, DuplicateRecipeException {

    if (recipeDao.existsByUrl(url)) {
      log.info("Recipe already exists for {}", url);
      throw new DuplicateRecipeException("Recipe already exists");
    }

    log.info("Downloading webpage {}", url);

    final Document document;
    try {
      document = Jsoup.connect(url)
          .userAgent(USER_AGENT)
          .referrer(REFERRER)
          .get();
    } catch (final IOException e) {
      log.error("Error getting page for recipe extraction {}", e.getMessage());
      throw e;
    }

    ExtractedRecipe recipe;
    try {
      recipe = recipeExtractionService.extractRecipe(document, url);
    } catch (final IllegalArgumentException e) {
      log.error("Error extracting recipe {}", e.getMessage());
      throw e;
    }

    if (recipe.getUrl() == null) {
      recipe.setUrl(url);
    }

    recipeDao.save(recipe);
    log.info("Saved recipe with {} ingredients and {} instructions",
        recipe.getExtractedRecipeIngredients().size(),
        recipe.getExtractedRecipeInstructions().size());

    return extractedRecipeToDtoConverter.convert(recipe);
  }
  public List<RecipeDto> getAllRecipes() {
    return recipeDao.findAll();
  }
}
