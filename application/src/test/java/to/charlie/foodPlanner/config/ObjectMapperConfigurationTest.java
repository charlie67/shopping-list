package to.charlie.foodPlanner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealUpdateDto;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the bean directly rather than through a controller. {@code MockMvcBuilders.standaloneSetup},
 * the style every controller test here uses, wires a Jackson converter of its own, so what a
 * controller test sees is MockMvc's date handling and not the application's. This is the test that
 * pins the behaviour of the bean the app actually serialises with - REST responses and websocket
 * frames alike.
 */
class ObjectMapperConfigurationTest {

	private final ObjectMapper objectMapper = new ObjectMapperConfiguration().objectMapper();

	@Test
	void serialize_whenLocalDate_thenIsoStringNotArray() throws Exception {
		// given / when
		final String json = objectMapper.writeValueAsString(LocalDate.of(2026, 9, 21));

		// then
		assertThat(json).isEqualTo("\"2026-09-21\"");
	}

	@Test
	void deserialize_whenIsoString_thenLocalDate() throws Exception {
		// given
		final String json = "{\"placement\":\"LEFTOVER\",\"plannedDate\":\"2026-09-23\"}";

		// when
		final PlannedMealUpdateDto update = objectMapper.readValue(json, PlannedMealUpdateDto.class);

		// then
		assertThat(update.getPlacement()).isEqualTo(MealPlacement.LEFTOVER);
		assertThat(update.getPlannedDate()).isEqualTo(LocalDate.of(2026, 9, 23));
	}
}
