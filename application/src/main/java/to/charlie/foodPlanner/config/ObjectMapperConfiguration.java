package to.charlie.foodPlanner.config;


import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperConfiguration {

	@Bean
	@Primary
	public JsonMapper objectMapper() {
		return JsonMapper.builder()
						.findAndAddModules()
						.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
						.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
						.build();
	}

	/**
	 * A deliberately forgiving mapper for JSON-LD read off other people's pages. Recipe blocks in the
	 * wild carry trailing commas, comments and raw control characters, and a block we cannot read is
	 * a recipe we cannot extract - so read leniently here rather than letting a stray comma cost us
	 * the page. Kept separate from the application mapper, which talks to our own APIs and should
	 * stay strict.
	 */
	@Bean("jsonLdObjectMapper")
	public JsonMapper jsonLdObjectMapper() {
		return JsonMapper.builder()
						.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
						.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
						.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
						.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
						.findAndAddModules()
						.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
						.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
						.build();
	}
}
