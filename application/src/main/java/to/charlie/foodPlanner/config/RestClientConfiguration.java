package to.charlie.foodPlanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfiguration {
	@Bean
	public RestClient restClient(final MappingJackson2HttpMessageConverter defaultConverter) {
		// Clone the default converter to avoid modifying the global Spring MVC one
		final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(defaultConverter.getObjectMapper());

		// Add application/octet-stream to the list of supported media types
		// this is required for JustTheRecipe
		final List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
		supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		converter.setSupportedMediaTypes(supportedMediaTypes);

		return RestClient.builder()
						.messageConverters(converters -> {
							converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
							converters.add(converter);
						})
						.build();

	}
}
