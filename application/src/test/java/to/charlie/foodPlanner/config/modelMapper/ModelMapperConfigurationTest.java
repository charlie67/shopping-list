package to.charlie.foodPlanner.config.modelMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedIngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeStepsDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelMapperConfigurationTest {

	private ModelMapper modelMapper;

	@BeforeEach
	void setUp() {
		modelMapper = new ModelMapperConfiguration().modelMapper();
	}

	private ExtractedRecipeDto buildDto() {
		return ExtractedRecipeDto.builder()
						.url("https://example.com/chilli")
						.name("Chilli")
						.description("A chilli")
						.imageUrl("https://example.com/chilli.jpg")
						.keywords("dinner, mexican")
						.cookTime("PT1H")
						.recipeYield("4")
						.calories("500")
						.ingredients(List.of(
										ExtractedIngredientDto.builder()
														.ingredientName("onion")
														.quantity(1)
														.unit(MeasurementUnit.UNKNOWN)
														.size("large")
														.quantityText("1")
														.fullText("1 large onion")
														.build(),
										ExtractedIngredientDto.builder()
														.ingredientName("beef mince")
														.quantity(500)
														.unit(MeasurementUnit.GRAMS)
														.quantityText("500")
														.fullText("500g beef mince")
														.build()))
						.instructions(List.of(
										ExtractedRecipeStepsDto.builder().text("Chop the onion").type("HowToStep").build(),
										ExtractedRecipeStepsDto.builder().text("Brown the mince").type("HowToStep").build()))
						.build();
	}

	@Test
	void map_whenDtoHasIngredients_thenNestsIngredientNameAndKeepsFields() {
		// when
		final ExtractedRecipe result = modelMapper.map(buildDto(), ExtractedRecipe.class);

		// then
		assertThat(result.getExtractedRecipeIngredients()).hasSize(2);

		final ExtractedRecipeIngredient onion = result.getExtractedRecipeIngredients().getFirst();
		assertThat(onion.getIngredient().getName()).isEqualTo("onion");
		assertThat(onion.getFullText()).isEqualTo("1 large onion");
		assertThat(onion.getQuantity()).isEqualTo(1);
		assertThat(onion.getUnit()).isEqualTo(MeasurementUnit.UNKNOWN);
		assertThat(onion.getSize()).isEqualTo("large");
		assertThat(onion.getQuantityText()).isEqualTo("1");

		assertThat(result.getExtractedRecipeIngredients().get(1).getUnit()).isEqualTo(MeasurementUnit.GRAMS);
	}

	@Test
	void map_whenDtoHasInstructions_thenKeepsThemInOrder() {
		// when
		final ExtractedRecipe result = modelMapper.map(buildDto(), ExtractedRecipe.class);

		// then
		assertThat(result.getExtractedRecipeInstructions())
						.extracting("text")
						.containsExactly("Chop the onion", "Brown the mince");
	}

	@Test
	void map_whenDtoHasScalarFields_thenTheyAreCarriedOver() {
		// when
		final ExtractedRecipe result = modelMapper.map(buildDto(), ExtractedRecipe.class);

		// then
		assertThat(result.getName()).isEqualTo("Chilli");
		assertThat(result.getUrl()).isEqualTo("https://example.com/chilli");
		assertThat(result.getDescription()).isEqualTo("A chilli");
		assertThat(result.getImageUrl()).isEqualTo("https://example.com/chilli.jpg");
		assertThat(result.getKeywords()).containsExactly("dinner", "mexican");
		assertThat(result.getCookTime()).isEqualTo("PT1H");
		assertThat(result.getRecipeYield()).isEqualTo("4");
		assertThat(result.getCalories()).isEqualTo("500");
	}

	@Test
	void map_whenDtoHasNoKeywords_thenKeywordsAreEmpty() {
		// given
		final ExtractedRecipeDto dto = buildDto();
		dto.setKeywords("");

		// when
		final ExtractedRecipe result = modelMapper.map(dto, ExtractedRecipe.class);

		// then
		assertThat(result.getKeywords()).isEmpty();
	}

	@Test
	void map_whenDtoIsMappedThroughToEntity_thenIngredientsAndStepsSurvive() {
		// when
		final ExtractedRecipe recipe = modelMapper.map(buildDto(), ExtractedRecipe.class);
		final RecipeEntity entity = modelMapper.map(recipe, RecipeEntity.class);

		// then
		assertThat(entity.getIngredients()).hasSize(2);
		assertThat(entity.getIngredients()).extracting("wholeText")
						.containsExactly("1 large onion", "500g beef mince");
		assertThat(entity.getSteps()).extracting("stepCount").containsExactly(1, 2);
		assertThat(entity.getKeywords()).isEqualTo("dinner, mexican");
	}

	@Test
	void map_whenRecipeIsMappedToDto_thenKeywordsAreFlattenedToAString() {
		// when
		final ExtractedRecipe recipe = modelMapper.map(buildDto(), ExtractedRecipe.class);
		final ExtractedRecipeDto dto = modelMapper.map(recipe, ExtractedRecipeDto.class);

		// then
		assertThat(dto.getKeywords()).isEqualTo("dinner, mexican");
		assertThat(dto.getIngredients()).extracting("ingredientName").containsExactly("onion", "beef mince");
		assertThat(dto.getInstructions()).extracting("text").containsExactly("Chop the onion", "Brown the mince");
	}
}
