package to.charlie.foodPlanner.domain.service.plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeYieldParserTest {

	private final RecipeYieldParser parser = new RecipeYieldParser();

	@ParameterizedTest
	@CsvSource({
					"4, 4",
					"2 servings, 2",
					"Serves 4, 4",
					// Pessimistic on purpose: over-estimating invents leftovers that do not exist.
					"Serves 6-8, 6",
					"1, 1",
					"20, 20",
					"Makes 12 portions, 12",
	})
	void parse_whenTheTextHoldsANumberOfDinners_thenReadsTheFirstRunOfDigits(final String yield,
	                                                                        final int expected) {
		// given / when
		final Integer servings = parser.parse(yield);

		// then
		assertThat(servings).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
					// A yield of 24 is biscuits, not dinners.
					"Makes 24 cookies",
					"21",
					"0",
					"0 servings",
					// The literal string a null Integer used to be stringified into.
					"null",
					"a dozen",
					"Serves a family",
					"   ",
	})
	void parse_whenTheTextHoldsNoUsableFigure_thenNull(final String yield) {
		// given / when / then
		assertThat(parser.parse(yield)).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void parse_whenNullOrEmpty_thenNull(final String yield) {
		// given / when / then
		assertThat(parser.parse(yield)).isNull();
	}

	@Test
	void parse_whenTheDigitsDoNotFitAnInt_thenNull() {
		// given / when / then
		assertThat(parser.parse("Serves 99999999999999999999")).isNull();
	}
}
