package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MeasurementUnitTest {

    @Test
    void convert_whenGramsInput_thenReturnsGrams() {
        assertThat(MeasurementUnit.convert("g")).isEqualTo(MeasurementUnit.GRAMS);
        assertThat(MeasurementUnit.convert("gram")).isEqualTo(MeasurementUnit.GRAMS);
        assertThat(MeasurementUnit.convert("GRAMS")).isEqualTo(MeasurementUnit.GRAMS);
    }

    @Test
    void convert_whenOzInput_thenReturnsOunces() {
        assertThat(MeasurementUnit.convert("oz")).isEqualTo(MeasurementUnit.OUNCES);
        assertThat(MeasurementUnit.convert("ounce")).isEqualTo(MeasurementUnit.OUNCES);
        assertThat(MeasurementUnit.convert("OUNCES")).isEqualTo(MeasurementUnit.OUNCES);
    }

    @Test
    void convert_whenTbspInput_thenReturnsTablespoons() {
        assertThat(MeasurementUnit.convert("tbsp")).isEqualTo(MeasurementUnit.TABLESPOONS);
        assertThat(MeasurementUnit.convert("tablespoon")).isEqualTo(MeasurementUnit.TABLESPOONS);
        assertThat(MeasurementUnit.convert("TABLESPOONS")).isEqualTo(MeasurementUnit.TABLESPOONS);
    }

    @Test
    void convert_whenTspInput_thenReturnsTeaspoons() {
        assertThat(MeasurementUnit.convert("tsp")).isEqualTo(MeasurementUnit.TEASPOONS);
        assertThat(MeasurementUnit.convert("teaspoon")).isEqualTo(MeasurementUnit.TEASPOONS);
        assertThat(MeasurementUnit.convert("TEASPOONS")).isEqualTo(MeasurementUnit.TEASPOONS);
    }

    @Test
    void convert_whenCupInput_thenReturnsCups() {
        assertThat(MeasurementUnit.convert("cup")).isEqualTo(MeasurementUnit.CUPS);
        assertThat(MeasurementUnit.convert("CUP")).isEqualTo(MeasurementUnit.CUPS);
    }

    @Test
    void convert_whenUnknownInput_thenReturnsUnknown() {
        // given
        String unknownUnit = "pinch";

        // when
        MeasurementUnit result = MeasurementUnit.convert(unknownUnit);

        // then
        assertThat(result).isEqualTo(MeasurementUnit.UNKNOWN);
    }

    @Test
    void convert_whenLowercaseInput_thenConvertsCorrectly() {
        // given / when / then
        assertThat(MeasurementUnit.convert("grams")).isEqualTo(MeasurementUnit.GRAMS);
        assertThat(MeasurementUnit.convert("oz")).isEqualTo(MeasurementUnit.OUNCES);
    }

    @Test
    void convert_whenInputWithLeadingAndTrailingWhitespace_thenTrimsAndConverts() {
        // given
        String paddedInput = "  g  ";

        // when
        MeasurementUnit result = MeasurementUnit.convert(paddedInput);

        // then
        assertThat(result).isEqualTo(MeasurementUnit.GRAMS);
    }
}
