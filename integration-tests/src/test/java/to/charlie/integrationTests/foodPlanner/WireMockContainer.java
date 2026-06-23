package to.charlie.integrationTests.foodPlanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class WireMockContainer extends GenericContainer<WireMockContainer> {

	private static final Logger logger = LoggerFactory.getLogger(WireMockContainer.class);
	private static final DockerImageName IMAGE = DockerImageName.parse("wiremock/wiremock:3.9.1");
	private static final int WIREMOCK_PORT = 8080;
	private static WireMockContainer container;

	private WireMockContainer() {
		super(IMAGE);
		withExposedPorts(WIREMOCK_PORT);
	}

	public static WireMockContainer getInstance() {
		if (container == null) {
			container = new WireMockContainer();
			container.start();
			final String baseUrl = "http://" + container.getHost() + ":" + container.getMappedPort(WIREMOCK_PORT);
			logger.info("WireMockContainer started with base URL: {}", baseUrl);
			System.setProperty("WIREMOCK_BASE_URL", baseUrl);
			System.setProperty("ingredient-breakdown.api.url", baseUrl + "/ingredient-breakdown");
			System.setProperty("just-the-recipe.url", baseUrl + "/just-the-recipe");
		}
		return container;
	}

	@Override
	public void start() {
		super.start();
		logger.info("WireMockContainer is starting.");
	}

	@Override
	public void stop() {
		//do nothing, JVM handles shut down
		logger.info("WireMockContainer is stopping.");
	}
}
