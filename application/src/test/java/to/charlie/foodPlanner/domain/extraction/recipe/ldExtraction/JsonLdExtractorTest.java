package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import to.charlie.foodPlanner.config.ObjectMapperConfiguration;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JsonLdExtractorTest {

	@Mock
	private IngredientBreakdownService ingredientBreakdownService;

	private JsonLdExtractor extractor;

	@BeforeEach
	void setUp() {
		final ObjectMapper objectMapper = new ObjectMapperConfiguration().objectMapper();
		extractor = new JsonLdExtractor(objectMapper,
						new JsonLdRecipeBuilder(ingredientBreakdownService, objectMapper));

		when(ingredientBreakdownService.convertIngredient(anyString())).thenAnswer(
						invocation -> List.of(ExtractedRecipeIngredient.builder().build()));
	}

	@Test
	void extract_whenGraphHoldsANodeWithMultipleTypes_thenStillFindsTheRecipe()
					throws RecipeExtractionFailed, IOException {
		// given a graph whose author node has an "@type" of ["Person", "Organization"]
		final Document document = documentWithJsonLd("recipeData/jsonld/example4");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://vidarbergum.com/recipe/cheats-lagman-uyghur-style-lamb-with-noodles/");

		// then
		assertThat(recipe.getName()).isEqualTo("Cheat's lagman – Uyghur inspired lamb with noodles");
		assertThat(recipe.getRecipeYield()).isEqualTo("6");
		assertThat(recipe.getRecipeCategory()).isEqualTo("Main Course");
		assertThat(recipe.getTotalTime()).isEqualTo("PT40M");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(16);
		assertThat(recipe.getExtractedRecipeInstructions()).hasSize(6);
		assertThat(recipe.getImageUrl()).isEqualTo(
						"https://vidarbergum.com/wp-content/uploads/2022/02/lagman-cheat-uyghur-noodles-lamb-11.jpg");
	}

	@Test
	void extract_whenTheRecipeHasNoNutritionBlock_thenNutritionIsLeftEmpty()
					throws RecipeExtractionFailed, IOException {
		// given
		final Document document = documentWithJsonLd("recipeData/jsonld/example4");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://vidarbergum.com/recipe/cheats-lagman-uyghur-style-lamb-with-noodles/");

		// then
		assertThat(recipe.getCalories()).isNull();
		assertThat(recipe.getProteinContent()).isNull();
		assertThat(recipe.getKeywords()).isEmpty();
	}

	@Test
	void extract_whenKeywordsAreACommaSeparatedString_thenTheyAreSplit()
					throws RecipeExtractionFailed {
		// given
		final Document document = Jsoup.parse("""
						<html><head><script type="application/ld+json">
						{"@context":"https://schema.org","@type":["Recipe","NewsArticle"],"name":"Soup",
						"keywords":"soup, winter ,warming","recipeYield":"4 servings",
						"recipeIngredient":"1 onion","recipeInstructions":"Boil it"}
						</script></head></html>""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Soup");
		assertThat(recipe.getKeywords()).containsExactly("soup", "winter", "warming");
		assertThat(recipe.getRecipeYield()).isEqualTo("4 servings");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(1);
		assertThat(recipe.getImageUrl()).isNull();
	}

	private Document documentWithJsonLd(final String resource) throws IOException {
		try (final var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
			final String jsonLd = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

			return Jsoup.parse("<html><head><script type=\"application/ld+json\">" + jsonLd
							+ "</script></head></html>");
		}
	}
}
