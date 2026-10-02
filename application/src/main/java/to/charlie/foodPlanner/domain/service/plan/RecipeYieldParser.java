package to.charlie.foodPlanner.domain.service.plan;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecipeYieldParser {

	private static final Pattern FIRST_NUMBER = Pattern.compile("\\d+");

	private static final int MIN_SERVINGS = 1;

	private static final int MAX_SERVINGS = 20;

	public Integer parse(final String recipeYield) {
		if (recipeYield == null || recipeYield.isBlank()) {
			return null;
		}

		final Matcher matcher = FIRST_NUMBER.matcher(recipeYield);
		if (!matcher.find()) {
			return null;
		}

		final int servings;
		try {
			servings = Integer.parseInt(matcher.group());
		} catch (final NumberFormatException e) {
			// A run of digits too long for an int. Whatever it is, it is not a number of dinners.
			return null;
		}

		return servings >= MIN_SERVINGS && servings <= MAX_SERVINGS ? servings : null;
	}
}
