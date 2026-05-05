package to.charlie.foodPlanner.domain.extraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

class IngredientExtractorTest {

    private IngredientExtractor ingredientExtractor;

    @BeforeEach
    void setUp() {
        ingredientExtractor = new IngredientExtractor(new QuantityExtractor());
    }

    @Test
    void convertIngredient_whenIngredientHasQuantityAndUnit_thenStripsQuantityUnitFromName() {
        // given
        String ingredient = "2 cups flour";

        // when
        ExtractedRecipeIngredient result = ingredientExtractor.convertIngredient(ingredient);

        // then
        assertThat(result.getIngredientName()).isEqualTo("flour");
        assertThat(result.getQuantity()).isCloseTo(2.0, within(0.001));
        assertThat(result.getUnit()).isEqualTo(MeasurementUnit.CUPS);
        assertThat(result.getFullText()).isEqualTo("2 cups flour");
    }

    @Test
    void convertIngredient_whenIngredientHasNoQuantityOrUnit_thenFullTextIsIngredientName() {
        // given
        String ingredient = "salt to taste";

        // when
        ExtractedRecipeIngredient result = ingredientExtractor.convertIngredient(ingredient);

        // then
        assertThat(result.getIngredientName()).isEqualTo("salt to taste");
        assertThat(result.getQuantity()).isEqualTo(0.0);
        assertThat(result.getUnit()).isEqualTo(MeasurementUnit.UNKNOWN);
        assertThat(result.getFullText()).isEqualTo("salt to taste");
    }

    @Test
    void convertIngredient_whenIngredientHasExtraSpaces_thenNormalizesSpaces() {
        // given
        String ingredient = "2  cups  flour";

        // when
        ExtractedRecipeIngredient result = ingredientExtractor.convertIngredient(ingredient);

        // then
        assertThat(result.getFullText()).isEqualTo("2 cups flour");
    }

    @Test
    void convertIngredient_whenIngredientHasLeadingAndTrailingWhitespace_thenTrims() {
        // given
        String ingredient = "  100 grams sugar  ";

        // when
        ExtractedRecipeIngredient result = ingredientExtractor.convertIngredient(ingredient);

        // then
        assertThat(result.getFullText()).isEqualTo("100 grams sugar");
        assertThat(result.getIngredientName()).isEqualTo("sugar");
    }

    @Test
    void convertIngredient_whenIngredientHasFractionQuantity_thenExtractsFractionCorrectly() {
        // given
        String ingredient = "1/4 tsp salt";

        // when
        ExtractedRecipeIngredient result = ingredientExtractor.convertIngredient(ingredient);

        // then
        assertThat(result.getQuantity()).isCloseTo(0.25, within(0.001));
        assertThat(result.getUnit()).isEqualTo(MeasurementUnit.TEASPOONS);
        assertThat(result.getIngredientName()).isEqualTo("salt");
    }
}
