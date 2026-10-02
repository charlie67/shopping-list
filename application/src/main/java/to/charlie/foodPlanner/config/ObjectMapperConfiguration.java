package to.charlie.foodPlanner.config;


import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperConfiguration {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	/**
	 * A deliberately forgiving mapper for JSON-LD read off other people's pages. Recipe blocks in the
	 * wild carry trailing commas, comments and raw control characters, and a block we cannot read is
	 * a recipe we cannot extract - so read leniently here rather than letting a stray comma cost us
	 * the page. Kept separate from the application mapper, which talks to our own APIs and should
	 * stay strict.
	 */
	@Bean("jsonLdObjectMapper")
	public ObjectMapper jsonLdObjectMapper() {
		final ObjectMapper objectMapper = JsonMapper.builder()
						.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
						.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
						.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
						.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
						.build();

		objectMapper.findAndRegisterModules();
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}
}
