package to.charlie.foodPlanner.infrastructure.rest.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe.dto.JustTheRecipeResponseDto;
import to.charlie.foodPlanner.domain.model.exception.JustTheRecipeExtractionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JustTheRecipeClient {

	private static final String USER_AGENT = "Samsung-fridge";
	private static final String BASE_URL = "https://www.justtherecipe.com/extractRecipeAtUrl";
	private final RestClient restClient;

	@Retryable(
					retryFor = JustTheRecipeExtractionException.class,
					maxAttempts = 3,
					backoff = @Backoff(delay = 1000),
					listeners = "justTheRecipeRetryListener"
	)
	public JustTheRecipeResponseDto getRecipe(final String recipeUrl) {
		log.info("Sending API request to JustTheRecipe for URL {}", recipeUrl);

		// https://www.justtherecipe.com/extractRecipeAtUrl?url={recipeUrl}
		final String targetUrl = UriComponentsBuilder.fromUriString(BASE_URL)
						.queryParam("url", recipeUrl)
						.build()
						.toUriString();

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
