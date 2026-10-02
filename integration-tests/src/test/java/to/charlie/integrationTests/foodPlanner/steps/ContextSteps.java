package to.charlie.integrationTests.foodPlanner.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.integrationTests.foodPlanner.utilities.Context;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextSteps {

	@Autowired
	private Context context;

	@Given("{string} is set to {string}")
	public void setClock(final String key, final String value) {
		context.set(key, value);
	}

	/**
	 * For the one behaviour that genuinely depends on today's date: a household change reshapes this
	 * week onwards and leaves earlier weeks alone, so a scenario testing it cannot write its dates down.
	 * A negative offset reads a date in the past.
	 */
	@Given("{string} is set to the date {int} days from today")
	public void setRelativeDate(final String key, final int days) {
		context.set(key, LocalDate.now().plusDays(days).toString());
	}

	@Then("{string} should be {string}")
	public void shouldBe(final String key, final String value) {
		assertEquals(value, context.get(key));
	}
}
