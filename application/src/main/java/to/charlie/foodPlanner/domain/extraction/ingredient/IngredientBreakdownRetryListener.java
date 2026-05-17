package to.charlie.foodPlanner.domain.extraction.ingredient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component("IngredientBreakdownRetryListener")
public class IngredientBreakdownRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> void onError(final RetryContext context,
	                                             final RetryCallback<T, E> callback, final Throwable throwable) {
		log.warn("Ingredient extractor API call failed. Retry attempt: {}. Error: {}",
						context.getRetryCount(),
						throwable.getMessage());
	}

}
