package to.charlie.foodPlanner.infrastructure.rest.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import to.charlie.foodPlanner.config.JustTheRecipeConfiguration;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto.JustTheRecipeResponseDto;
import to.charlie.foodPlanner.domain.model.exception.JustTheRecipeExtractionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JustTheRecipeClient {

	private static final String USER_AGENT = "Samsung-fridge";

	private final RestClient restClient;
	private final JustTheRecipeConfiguration justTheRecipeConfiguration;

	@Retryable(
					includes = JustTheRecipeExtractionException.class,
					maxRetries = 2,
					delay = 1000
	)
	public JustTheRecipeResponseDto getRecipe(final String recipeUrl) {
		// https://www.justtherecipe.com/extractRecipeAtUrl?url={recipeUrl}
		final String targetUrl = UriComponentsBuilder.fromUriString(justTheRecipeConfiguration.getUrl())
						.queryParam("url", recipeUrl)
						.build()
						.toUriString();

		log.info("Sending API request {} to JustTheRecipe for URL {}", targetUrl, recipeUrl);

		return restClient
						.get()
						.uri(targetUrl)
						.header("User-Agent", USER_AGENT)
						.retrieve()
						.onStatus(status -> status.value() != 200, (request, response) -> {
							throw new JustTheRecipeExtractionException(
											"Failed to fetch recipe from JustTheRecipe API. Status code: "
															+ response.getStatusCode().value());
						})
						.body(JustTheRecipeResponseDto.class);
	}
}
