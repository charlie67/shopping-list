package to.charlie.foodPlanner.domain.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.infrastructure.dal.dao.RecipeDao;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

  public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:149.0) Gecko/20100101 Firefox/149.0";
  public static final String REFERRER = "https://www.google.com";

  private final RecipeDao recipeDao;
  private final RecipeExtractionService recipeExtractionService;
  private final ModelMapper modelMapper;

  public ExtractedRecipeDto extractRecipeFromUrl(final String url, final boolean saveRecipe)
      throws IOException, DuplicateRecipeException {

    if (saveRecipe && recipeDao.existsByUrl(url)) {
      log.info("Recipe for url {} already exists in database", url);
      return modelMapper.map(recipeDao.findByUrl(url), ExtractedRecipeDto.class);
    }

    log.info("Downloading webpage {}", url);

    final Document document;
    try {
      document = Jsoup.connect(url)
          .userAgent(USER_AGENT)
          .referrer(REFERRER)
          .get();
    } catch (final IOException | IllegalArgumentException e) {
      log.error("Error getting page for recipe extraction {}", e.getMessage());
      throw e;
    }

    final ExtractedRecipe recipe;
    try {
      recipe = recipeExtractionService.extractRecipe(document, url);
    } catch (final IllegalArgumentException e) {
      log.error("Error extracting recipe {}", e.getMessage());
      throw e;
    }

    if (recipe.getUrl() == null) {
      recipe.setUrl(url);
    }

    log.info("extracted recipe with {} ingredients and {} instructions",
        recipe.getExtractedRecipeIngredients().size(),
        recipe.getExtractedRecipeInstructions().size());

    if (saveRecipe) {
      final RecipeEntity savedRecipe = recipeDao.save(recipe);

      log.info("Saved recipe with {} ingredients and {} instructions",
          recipe.getExtractedRecipeIngredients().size(),
          recipe.getExtractedRecipeInstructions().size());

      return modelMapper.map(savedRecipe, ExtractedRecipeDto.class);
    } else {
      return modelMapper.map(recipe, ExtractedRecipeDto.class);
    }
  }

  public List<ExtractedRecipeDto> getAllRecipes() {
    return recipeDao.findAll();
  }

  public Page<ExtractedRecipeDto> getRecipePage(final int page) {
    return recipeDao.findPage(page);
  }
}
