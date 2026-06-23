package to.charlie.integrationTests.foodPlanner.steps;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import to.charlie.integrationTests.foodPlanner.WireMockContainer;
import to.charlie.integrationTests.foodPlanner.utilities.DataLoader;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Slf4j
public class WiremockSteps {

	@Value("${WIREMOCK_BASE_URL}")
	private String wiremockBaseUrl;

	@Autowired
	public DataLoader loader;

	private final WireMock wireMockClient;

	public WiremockSteps() {
		final WireMockContainer container = WireMockContainer.getInstance();

		this.wireMockClient = new WireMock(container.getHost(), container.getMappedPort(8080));
	}

	@Given("recipe URL {string} is set to return the data from file {string}")
	public void recipeURLIsSetToReturnTheDataFromFile(final String url, final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(get(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(200)
										.withHeader("Content-Type", "text/html")
										.withBody(content)));

		log.info("Stubbed URL: {} with data from {}", url, bodyFile);
	}

	@Given("ingredient breakdown service is set to return the data from file {string} for ingredient {string}")
	public void ingredientBreakdownURLIsSetToReturnTheDataFromFile(final String bodyFile, final String ingredientName) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(
						get(urlPathEqualTo("/ingredient-breakdown"))
										.withQueryParam("ingredient", equalTo(ingredientName))
										.willReturn(aResponse()
														.withStatus(200)
														.withHeader("Content-Type", "application/json")
														.withBody(content)));

		log.info("Stubbed ingredient breakdown URL: {} with data from {}", ingredientName, bodyFile);
	}

	@Given("ingredient breakdown service is set to return the data from file {string} for any ingredient")
	public void ingredientBreakdownIsSetToReturnTheDataFromFileForAnyIngredient(final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(
						get(urlPathEqualTo("/ingredient-breakdown"))
										.willReturn(aResponse()
														.withStatus(200)
														.withHeader("Content-Type", "application/json")
														.withBody(content)));

		log.info("Stubbed ingredient breakdown URL for any ingredient with data from {}", bodyFile);
	}
}
