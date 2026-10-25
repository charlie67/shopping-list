package to.charlie.integrationTests.foodPlanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer {

  private static final Logger logger = LoggerFactory.getLogger(PostgresContainer.class);
  private static final String IMAGE_VERSION = "postgres:15.2-alpine";
  private static PostgresContainer container;

  private PostgresContainer() {
    super(IMAGE_VERSION);
    withUsername("food-planner");
    withPassword("food-planner");
    withDatabaseName("food-planner");
  }

  public static PostgresContainer getInstance() {
    if (container == null) {
      container = new PostgresContainer();
      container.start();
      logger.info("PostgresContainer started with URL: {}", container.getJdbcUrl());
      System.setProperty("DB_URL", container.getJdbcUrl());
      System.setProperty("DB_USERNAME", container.getUsername());
      System.setProperty("DB_PASSWORD", container.getPassword());
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    logger.info("PostgresContainer is starting.");
  }

  @Override
  public void stop() {
    //do nothing, JVM handles shut down
    logger.info("PostgresContainer is stopping.");
  }
}