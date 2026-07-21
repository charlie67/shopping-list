package to.charlie.foodPlanner.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.infrastructure.dal.dao.RecipeDao;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:149.0) Gecko/20100101 Firefox/149.0";
	public static final String REFERRER = "https://www.google.com";

	private final RecipeDao recipeDao;
	private final RecipeExtractionService recipeExtractionService;
	private final ModelMapper modelMapper;

	public ExtractedRecipeDto extractRecipeFromUrl(final String url)
					throws IOException, DuplicateRecipeException {

		if (recipeDao.existsByUrl(url)) {
			log.info("Recipe for url {} already exists in database", url);
			return recipeDao.findDtoByUrl(url)
							.orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
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

		return modelMapper.map(recipe, ExtractedRecipeDto.class);
	}

	public Page<ExtractedRecipeDto> getRecipePage(final int page) {
		return recipeDao.findPage(page);
	}

	public Optional<ExtractedRecipeDto> getRecipeById(final UUID id) {
		return recipeDao.findById(id);
	}

	public ExtractedRecipeDto saveRecipe(final ExtractedRecipeDto extractedRecipe) {
		// The UI only offers to save a recipe it has just extracted, but nothing stops the same URL being
		// submitted twice, so fall back to the stored copy rather than inserting a second row.
		if (extractedRecipe.getUrl() != null && recipeDao.existsByUrl(extractedRecipe.getUrl())) {
			log.info("Recipe for url {} already exists in database, not saving again", extractedRecipe.getUrl());
			return recipeDao.findDtoByUrl(extractedRecipe.getUrl())
							.orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
		}

		final ExtractedRecipe recipe = modelMapper.map(extractedRecipe, ExtractedRecipe.class);

		final RecipeEntity savedRecipe = recipeDao.save(recipe);

		log.info("Saved recipe with {} ingredients and {} instructions",
						recipe.getExtractedRecipeIngredients().size(),
						recipe.getExtractedRecipeInstructions().size());

		return modelMapper.map(savedRecipe, ExtractedRecipeDto.class);
	}

	public ExtractedRecipeDto updateRecipe(final UUID id, final ExtractedRecipeDto extractedRecipe) {
		final ExtractedRecipe recipe = modelMapper.map(extractedRecipe, ExtractedRecipe.class);
		recipe.setId(id);

		final RecipeEntity savedRecipe = recipeDao.save(recipe);

		log.info("Update recipe with ID {}", recipe.getId());

		return modelMapper.map(savedRecipe, ExtractedRecipeDto.class);
	}

	@Transactional
	public void deleteRecipeById(final UUID id) {
		recipeDao.delete(id);
	}
}
