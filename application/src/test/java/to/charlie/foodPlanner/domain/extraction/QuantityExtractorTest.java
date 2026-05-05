package to.charlie.foodPlanner.domain.extraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.IngredientMeasurement;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class QuantityExtractorTest {

	private QuantityExtractor quantityExtractor;

	@BeforeEach
	void setUp() {
		quantityExtractor = new QuantityExtractor();
	}

	@Test
	void extractQuantityFromIngredient_whenIntegerQuantityAndGramsUnit_thenReturnsMeasurement() {
		// given
		final String ingredient = "200 grams butter";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(200.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.GRAMS);
		assertThat(result.quantityUnitText()).isEqualTo("200 grams");
	}

	@Test
	void extractQuantityFromIngredient_whenFractionQuantity_thenReturnsDecimalQuantity() {
		// given
		final String ingredient = "1/2 cup flour";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isCloseTo(0.5, within(0.001));
		assertThat(result.unit()).isEqualTo(MeasurementUnit.CUPS);
	}

	@Test
	void extractQuantityFromIngredient_whenMixedFraction_thenReturnsSummedQuantity() {
		// given
		final String ingredient = "2 1/2 cups sugar";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isCloseTo(2.5, within(0.001));
		assertThat(result.unit()).isEqualTo(MeasurementUnit.CUPS);
	}

	@Test
	void extractQuantityFromIngredient_whenNoUnitOrQuantity_thenReturnsBlankMeasurement() {
		// given
		final String ingredient = "pinch of salt";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(0.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.UNKNOWN);
		assertThat(result.quantityUnitText()).isEmpty();
	}

	@Test
	void extractQuantityFromIngredient_whenTbspUnit_thenReturnsTablespoonsUnit() {
		// given
		final String ingredient = "2 tbsp olive oil";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(2.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.TABLESPOONS);
	}

	@Test
	void extractQuantityFromIngredient_whenTspUnit_thenReturnsTeaspoonsUnit() {
		// given
		final String ingredient = "1 tsp vanilla extract";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(1.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.TEASPOONS);
	}

	@Test
	void extractQuantityFromIngredient_whenOzUnit_thenReturnsOuncesUnit() {
		// given
		final String ingredient = "4 oz cream cheese";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(4.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.OUNCES);
	}

	@Test
	void extractQuantityFromIngredient_whenUpperCaseUnit_thenMatchesCaseInsensitively() {
		// given
		final String ingredient = "3 CUPS milk";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(3.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.CUPS);
	}

	@Test
	void extractQuantityFromIngredient_whenTablespoonPluralUnit_thenReturnsTablespoonsUnit() {
		// given
		final String ingredient = "3 tablespoons butter";

		// when
		final IngredientMeasurement result = quantityExtractor.extractQuantityFromIngredient(ingredient);

		// then
		assertThat(result.quantity()).isEqualTo(3.0);
		assertThat(result.unit()).isEqualTo(MeasurementUnit.TABLESPOONS);
	}
}
