package to.charlie.integrationTests.foodPlanner;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "to.charlie.foodPlanner")
@EnableAutoConfiguration
public class TestConfig {

}
