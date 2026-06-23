package to.charlie.foodPlanner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "just-the-recipe")
@Getter
@Setter
public class JustTheRecipeConfiguration {
	private String url;
}
