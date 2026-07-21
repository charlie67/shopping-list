package to.charlie.integrationTests.foodPlanner.utilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substitutes {@code {placeholder}} tokens in the URLs written in feature files.
 *
 * <p>A placeholder is the {@link Context} key verbatim, so {@code {RECIPE_ID}} reads
 * {@code RECIPE_ID}: the name a scenario stores is the name it references, with nothing rewritten
 * in between. A scenario can therefore introduce a new variable purely in Gherkin — store it with
 * the "I store the value of ... as ..." step and reference it — without this class changing.
 *
 * <p>Every {@code {token}} in a URL is a variable, and one that cannot be resolved is an error, so
 * no request ever carries a literal {@code {...}} to the server.
 */
@Component
public class UrlVariableResolver {

	// Matches a placeholder in any style, not just the upper snake case the keys use, so that a
	// mistyped name fails as an unresolved variable rather than falling through the regex and being
	// sent as a literal part of the URL.
	private static final Pattern VARIABLE = Pattern.compile("\\{([A-Za-z0-9_-]+)}");

	private final Context context;

	public UrlVariableResolver(final Context context,
														 @Value("${WIREMOCK_BASE_URL}") final String wiremockBaseUrl) {
		this.context = context;
		// WireMock's port is only known once its container is up, so seed it here rather than making
		// every scenario store it. That keeps lookup to the single context-backed path below.
		context.set("WIREMOCK_URL", wiremockBaseUrl);
	}

	public String resolve(final String url) {
		return VARIABLE.matcher(url)
						.replaceAll(match -> Matcher.quoteReplacement(valueOf(match.group(1))));
	}

	private String valueOf(final String key) {
		final String value = context.get(key);

		if (value == null) {
			throw new IllegalStateException("No value for URL variable '{" + key + "}': nothing is stored"
							+ " in the context under that name earlier in the scenario.");
		}

		return value;
	}
}
