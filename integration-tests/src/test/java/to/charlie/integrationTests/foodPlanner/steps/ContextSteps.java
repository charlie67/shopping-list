package to.charlie.integrationTests.foodPlanner.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.integrationTests.foodPlanner.utilities.Context;

public class ContextSteps {

  @Autowired
  private Context context;

  @Given("{string} is set to {string}")
  public void setClock(final String key, final String value) {
    context.set(key, value);
  }

  @Then("{string} should be {string}")
  public void shouldBe(final String key, final String value) {
    assertEquals(value, context.get(key));
  }
}
