package to.charlie.integrationTests.foodPlanner;

import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import to.charlie.foodPlanner.ShoppingListApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = {ShoppingListApplication.class,
    TestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

  @BeforeAll
  public static void beforeAll() {
    PostgresContainer.getInstance();
  }
}