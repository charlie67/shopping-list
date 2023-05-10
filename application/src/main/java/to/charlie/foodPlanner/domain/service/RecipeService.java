package to.charlie.foodPlanner.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.dal.dao.RecipeDao;
import to.charlie.foodPlanner.domain.dal.dao.RecipeIngredientDao;
import to.charlie.foodPlanner.domain.extraction.ExtractorFactory;
import to.charlie.foodPlanner.domain.extraction.ExtractorHolder;
import to.charlie.foodPlanner.domain.model.dto.RecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedReceipeDto;
import to.charlie.foodPlanner.domain.model.entity.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeStep;
import to.charlie.foodPlanner.domain.model.mapping.ExtractedIngredientMapper;
import to.charlie.foodPlanner.domain.model.mapping.ExtractedRecipeStepsMapper;
import to.charlie.foodPlanner.domain.model.mapping.RecipeIngredientMapper;
import to.charlie.foodPlanner.domain.model.mapping.RecipeMapper;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

	private final RecipeDao recipeDao;
	private final RecipeMapper recipeMapper;
	private final RecipeIngredientDao recipeIngredientDao;

	private final RecipeIngredientMapper recipeIngredientMapper;

	public ExtractedReceipeDto extractRecipeFromUrl(final String url) throws IOException {
		final Document document;

		try {
			document = Jsoup.connect(url).get();
		} catch (final IOException e) {
			log.info("Error getting page for recipe extraction", e);
			throw e;
		}

		final String title = document.title();
		final ExtractorHolder extractorHolder = ExtractorFactory.getExtractor(url);

		final List<ExtractedIngredient> ingredients = extractorHolder.extractIngredients(document);
		final List<ExtractedRecipeStep> recipeSteps = extractorHolder.extractRecipeSteps(document);

		return ExtractedReceipeDto.builder()
						.recipeName(title)
						.url(url)
						.steps(ExtractedRecipeStepsMapper.map(recipeSteps))
						.ingredients(ExtractedIngredientMapper.map(ingredients))
						.build();
	}

	public RecipeDto saveNewRecipe(final RecipeDto recipe) {
		final Set<RecipeIngredientEntity> recipeIngredientEntities = recipe.getIngredients().stream().map(recipeIngredientMapper::dtoToEntity).collect(Collectors.toSet());

		final RecipeEntity recipeEntity = recipeMapper.dtoToEntity(recipe);
		recipeEntity.setIngredients(recipeIngredientEntities);
		recipeIngredientEntities.forEach(ingredient -> ingredient.setRecipe(recipeEntity));

		final RecipeEntity savedRecipeEntity = recipeDao.save(recipeEntity);
		return recipeMapper.entityToDto(savedRecipeEntity);
	}
}
