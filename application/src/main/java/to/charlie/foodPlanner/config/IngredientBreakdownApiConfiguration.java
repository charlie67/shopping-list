package to.charlie.foodPlanner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ingredient-breakdown.api")
@Getter
@Setter
public class IngredientBreakdownApiConfiguration {
	private String url;
}
