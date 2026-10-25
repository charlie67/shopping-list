package to.charlie.foodPlanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfiguration {
	@Bean
	public RestClient restClient(final JsonMapper jsonMapper) {
		// Build a dedicated converter to avoid modifying the global Spring MVC one
		final JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

		// Add application/octet-stream to the list of supported media types
		// this is required for JustTheRecipe
		final List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
		supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		converter.setSupportedMediaTypes(supportedMediaTypes);

		return RestClient.builder()
						.configureMessageConverters(converters -> converters.withJsonConverter(converter))
						.build();

	}
}
