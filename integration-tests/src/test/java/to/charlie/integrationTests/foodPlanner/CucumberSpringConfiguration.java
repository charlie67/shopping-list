package to.charlie.integrationTests.foodPlanner;

import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import to.charlie.foodPlanner.ShoppingListApplication;
import to.charlie.integrationTests.foodPlanner.utilities.Ports;

@CucumberContextConfiguration
@SpringBootTest(classes = {ShoppingListApplication.class,
				TestConfig.class}, webEnvironment = WebEnvironment.DEFINED_PORT, properties = {
				"server.port=" + Ports.SPRING,
})
public class CucumberSpringConfiguration {

	@BeforeAll
	public static void beforeAll() {
		PostgresContainer.getInstance();
		WireMockContainer.getInstance();
	}
}