package to.charlie.foodPlanner.domain.extraction.ingredient.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientBreakdownDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void deserialize_whenJsonMatchesPayload_thenMapsAllFields() throws Exception {
		// given
		final String json = """
						{
						  "ingredient": [
						    {
						      "name": "onion"
						    }
						  ],
						  "original": "100g large onion finely chopped",
						  "preparation": "finely chopped",
						  "quantity": [
						    {
						      "quantity": 100.0,
						      "text": "100 g",
						      "unit": "gram"
						    }
						  ],
						  "size": "large"
						}
						""";

		// when
		final IngredientBreakdownDto result = objectMapper.readValue(json, IngredientBreakdownDto.class);

		// then
		assertThat(result.original()).isEqualTo("100g large onion finely chopped");
		assertThat(result.preparation()).isEqualTo("finely chopped");
		assertThat(result.size()).isEqualTo("large");
		assertThat(result.ingredients()).hasSize(1);
		assertThat(result.ingredients().getFirst().name()).isEqualTo("onion");
		assertThat(result.quantities()).hasSize(1);
		assertThat(result.quantities().getFirst().quantity()).isEqualTo(100.0);
		assertThat(result.quantities().getFirst().text()).isEqualTo("100 g");
		assertThat(result.quantities().getFirst().unit()).isEqualTo("gram");
	}
}


