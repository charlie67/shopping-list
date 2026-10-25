package to.charlie.foodPlanner.domain.extraction.ingredient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;
import to.charlie.foodPlanner.domain.model.exception.BreakdownIngredientException;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.infrastructure.rest.clients.IngredientBreakdownClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientBreakdownServiceTest {

	@Mock
	private IngredientBreakdownClient ingredientBreakdownClient;

	private IngredientBreakdownService service;

	@BeforeEach
	void setUp() {
		service = new IngredientBreakdownService(ingredientBreakdownClient);
	}

	@Test
	void convertIngredient_whenQuantityIsRange_thenKeepsQuantityMax() {
		// given
		final String ingredient = "1-1.2kg/2lb 4oz-2lb 10oz whole chicken";
		when(ingredientBreakdownClient.extractIngredients(ingredient)).thenReturn(breakdown(
						new IngredientBreakdownDto.QuantityItemDto(1.0, 1.2, "1-1.2 kg", "kilogram")));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getQuantity()).isEqualTo(1.0);
		assertThat(result.getFirst().getQuantityMax()).isEqualTo(1.2);
		assertThat(result.getFirst().getQuantityText()).isEqualTo("1-1.2 kg");
	}

	@Test
	void convertIngredient_whenQuantityMaxEqualsQuantity_thenQuantityMaxIsNull() {
		// given
		final String ingredient = "500g whole chicken";
		when(ingredientBreakdownClient.extractIngredients(ingredient)).thenReturn(breakdown(
						new IngredientBreakdownDto.QuantityItemDto(500.0, 500.0, "500 g", "gram")));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result.getFirst().getQuantity()).isEqualTo(500.0);
		assertThat(result.getFirst().getQuantityMax()).isNull();
	}

	@Test
	void convertIngredient_whenQuantityMaxMissing_thenQuantityMaxIsNull() {
		// given
		final String ingredient = "500g whole chicken";
		when(ingredientBreakdownClient.extractIngredients(ingredient)).thenReturn(breakdown(
						new IngredientBreakdownDto.QuantityItemDto(500.0, null, "500 g", "gram")));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result.getFirst().getQuantityMax()).isNull();
	}

	@Test
	void convertIngredient_whenBreakdownServiceFails_thenKeepsIngredientUnparsed() {
		// given
		final String ingredient = "a pinch of something odd";
		when(ingredientBreakdownClient.extractIngredients(ingredient))
						.thenThrow(new BreakdownIngredientException("Status code: 500"));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getIngredient().getName()).isEqualTo(ingredient);
		assertThat(result.getFirst().getFullText()).isEqualTo(ingredient);
		assertThat(result.getFirst().getQuantity()).isNull();
		assertThat(result.getFirst().getQuantityText()).isNull();
	}

	@Test
	void convertIngredient_whenBreakdownServiceUnreachable_thenKeepsIngredientUnparsed() {
		// given
		final String ingredient = "1 large onion";
		when(ingredientBreakdownClient.extractIngredients(ingredient))
						.thenThrow(new ResourceAccessException("Connection refused"));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getIngredient().getName()).isEqualTo(ingredient);
	}

	@Test
	void convertIngredient_whenNoIngredientFound_thenKeepsIngredientUnparsed() {
		// given
		final String ingredient = "to serve";
		when(ingredientBreakdownClient.extractIngredients(ingredient))
						.thenReturn(new IngredientBreakdownDto(List.of(), ingredient, null, List.of(), null, null, null));

		// when
		final List<ExtractedRecipeIngredient> result = service.convertIngredient(ingredient);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getIngredient().getName()).isEqualTo(ingredient);
		assertThat(result.getFirst().getFullText()).isEqualTo(ingredient);
	}

	@Test
	void rangeMax_whenMaxBelowQuantity_thenReturnsNull() {
		assertThat(IngredientBreakdownService.rangeMax(2.0, 1.2)).isNull();
	}

	@Test
	void rangeMax_whenQuantityMissing_thenReturnsNull() {
		assertThat(IngredientBreakdownService.rangeMax(null, 1.2)).isNull();
	}

	private static IngredientBreakdownDto breakdown(final IngredientBreakdownDto.QuantityItemDto quantity) {
		return new IngredientBreakdownDto(
						List.of(new IngredientBreakdownDto.IngredientItemDto("whole chicken")),
						null, null, List.of(quantity), null, null, null);
	}
}
