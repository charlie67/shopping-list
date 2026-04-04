package to.charlie.integrationTests.foodPlanner;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import to.charlie.integrationTests.foodPlanner.utilities.Context;
import to.charlie.integrationTests.foodPlanner.utilities.DataLoader;

@Configuration
@ComponentScan(basePackages = {"to.charlie.foodPlanner", "to.charlie.integrationTests.foodPlanner"})
@EnableAutoConfiguration
public class TestConfig {

  @Bean
  public Context context() {
    return new Context();
  }

  @Bean
  public DataLoader loader(final Context context) {
    return new DataLoader(context);
  }
}
