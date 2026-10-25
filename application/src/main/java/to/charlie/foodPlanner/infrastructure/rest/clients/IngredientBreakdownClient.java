package to.charlie.foodPlanner.infrastructure.rest.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import to.charlie.foodPlanner.config.IngredientBreakdownApiConfiguration;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;
import to.charlie.foodPlanner.domain.model.exception.BreakdownIngredientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngredientBreakdownClient {

	private final RestClient restClient;
	private final IngredientBreakdownApiConfiguration configuration;

	@Retryable(
					includes = BreakdownIngredientException.class,
					maxRetries = 2,
					delay = 1000
	)
	public IngredientBreakdownDto extractIngredients(final String ingredientString) {
		final String targetUrl = UriComponentsBuilder.fromUriString(configuration.getUrl())
						.queryParam("ingredient", ingredientString)
						.build()
						.toUriString();
		log.info("Sending API request {} to ingredient breakdown service for ingredient {}", targetUrl, ingredientString);

		return restClient.get().uri(targetUrl).retrieve()
						.onStatus(status -> status.value() != 200, (request, response) -> {
							throw new BreakdownIngredientException(
											"Failed to breakdown ingredient from the ingredient breakdown API. Status code: "
															+ response.getStatusCode().value());
						})
						.body(IngredientBreakdownDto.class);
	}
}
