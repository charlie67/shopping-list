package to.charlie.foodPlanner.domain.extraction.recipe.justtherecipe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.retry.MethodRetryEvent;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.infrastructure.rest.clients.JustTheRecipeClient;

@Slf4j
@Component
public class JustTheRecipeRetryListener {

	@EventListener
	public void onRetryEvent(final MethodRetryEvent event) {
		if (event.getMethod().getDeclaringClass() != JustTheRecipeClient.class) {
			return;
		}

		if (event.isRetryAborted() && event.getFailure() instanceof final RetryException e) {
			log.warn("JustTheRecipe API call failed after {} retries. Error: {}",
							e.getRetryCount(),
							e.getLastException().getMessage());
		} else {
			log.warn("JustTheRecipe API call failed. Error: {}", event.getFailure().getMessage());
		}
	}
}
