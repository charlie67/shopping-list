package to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto.IngredientDto;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto.InstructionDto;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto.JustTheRecipeResponseDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;
import to.charlie.foodPlanner.infrastructure.rest.clients.JustTheRecipeClient;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JustTheRecipeExtractorTest {

	@Mock
	private JustTheRecipeClient justTheRecipeClient;

	@Mock
	private IngredientBreakdownService mockIngredientBreakdownService;

	private JustTheRecipeExtractor extractor;

	@BeforeEach
	void setUp() {
		extractor = new JustTheRecipeExtractor(justTheRecipeClient, mockIngredientBreakdownService);
	}

	@Test
	void findMainRecipeImage_whenOgImageMetaTagPresent_thenReturnsOgImageUrl() {
		// given
		final Document document = Jsoup.parse(
						"<html><head><meta property=\"og:image\" content=\"https://example.com/image.jpg\"></head></html>");

		// when
		final String result = extractor.findMainRecipeImage(document);

		// then
		assertThat(result).isEqualTo("https://example.com/image.jpg");
	}

	@Test
	void findMainRecipeImage_whenNoOgImageButTwitterImagePresent_thenReturnsTwitterImageUrl() {
		// given
		final Document document = Jsoup.parse(
						"<html><head><meta name=\"twitter:image\" content=\"https://example.com/twitter.jpg\"></head></html>");

		// when
		final String result = extractor.findMainRecipeImage(document);

		// then
		assertThat(result).isEqualTo("https://example.com/twitter.jpg");
	}

	@Test
	void findMainRecipeImage_whenNoImages_thenReturnsNull() {
		// given
		final Document document = Jsoup.parse("<html><head></head></html>");

		// when
		final String result = extractor.findMainRecipeImage(document);

		// then
		assertThat(result).isNull();
	}

	@Test
	void findMainRecipeImage_whenOgImageHasEmptyContent_thenFallsBackToTwitterImage() {
		// given
		final Document document = Jsoup.parse(
						"<html><head>" +
										"<meta property=\"og:image\" content=\"\">" +
										"<meta name=\"twitter:image\" content=\"https://example.com/twitter.jpg\">" +
										"</head></html>");

		// when
		final String result = extractor.findMainRecipeImage(document);

		// then
		assertThat(result).isEqualTo("https://example.com/twitter.jpg");
	}

	@Test
	void extract_whenClientReturnsValidResponse_thenMapsFieldsCorrectly() {
		// given
		final String url = "https://example.com/recipe";
		final ExtractedRecipeIngredient extractedIngredient = ExtractedRecipeIngredient.builder()
						.fullText("flour")
						.ingredientName("flour")
						.quantity(0.0)
						.unit(MeasurementUnit.UNKNOWN)
						.build();

		final JustTheRecipeResponseDto response = new JustTheRecipeResponseDto(
						"1.0",
						"abc123",
						"Banana Bread",
						url,
						4,
						60L,
						List.of(),
						List.of(),
						List.of(),
						List.of(new IngredientDto("flour", null, null, null, null, null)),
						List.of(new InstructionDto("Mix ingredients", "HowToStep")),
						"source"
		);

		final Document document = Jsoup.parse(
						"<html><head><meta property=\"og:image\" content=\"https://example.com/img.jpg\"></head></html>");

		when(justTheRecipeClient.getRecipe(url)).thenReturn(response);
		when(mockIngredientBreakdownService.convertIngredient("flour")).thenReturn(Collections.singletonList(extractedIngredient));

		// when
		final ExtractedRecipe result = extractor.extract(document, url);

		// then
		assertThat(result.getName()).isEqualTo("Banana Bread");
		assertThat(result.getUrl()).isEqualTo(url);
		assertThat(result.getRecipeYield()).isEqualTo("4");
		assertThat(result.getTotalTime()).isEqualTo("60");
		assertThat(result.getExtractedRecipeIngredients()).hasSize(1);
		assertThat(result.getExtractedRecipeInstructions()).hasSize(1);
		assertThat(result.getExtractedRecipeInstructions().get(0).getText()).isEqualTo("Mix ingredients");
		assertThat(result.getImageUrl()).isEqualTo("https://example.com/img.jpg");
	}
}
