package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Coercion for JSON-LD values, where any property may legally arrive as a scalar, an array, a
 * {@code {"@value": ...}} wrapper, a language tagged list or an {@code @id} reference to a node
 * somewhere else in the document. Reading every field through here is what keeps a legal but
 * unusual page from failing the whole extraction.
 */
public final class JsonLdValues {

	private static final int MAX_DEPTH = 6;

	/** Keys an object may hide its scalar value behind, in the order we prefer them. */
	private static final String[] VALUE_KEYS = {"@value", "name", "text"};
	private static final String[] URL_KEYS = {"url", "contentUrl", "@value"};

	private static final Pattern HTML_TAG = Pattern.compile("</?[a-zA-Z][^>]*>");
	/** NBSP, zero width space/non-joiner/joiner and the BOM, all of which break ingredient matching. */
	private static final Pattern INVISIBLE = Pattern.compile("[\\u00a0\\u200b\\u200c\\u200d\\ufeff]");
	private static final Pattern HORIZONTAL_SPACE = Pattern.compile("[ \\t\\x0B\\f\\r]+");
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private static final Pattern ISO_DURATION = Pattern.compile("^P(?=.)(\\d+[YMWD])*(T(\\d+[HMS])+)?$");
	private static final Pattern CLOCK_DURATION = Pattern.compile("^(\\d{1,2}):([0-5]\\d)(?::([0-5]\\d))?$");
	private static final Pattern DURATION_PART =
					Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(days?|d|hours?|hrs?|h|minutes?|mins?|m|seconds?|secs?|s)\\b",
									Pattern.CASE_INSENSITIVE);
	private static final Pattern BARE_NUMBER = Pattern.compile("^\\d+(?:\\.\\d+)?$");
	private static final Pattern RANGE = Pattern.compile("\\d\\s*(?:-|\\u2013|\\u2014|to)\\s*(?=\\d)");

	private JsonLdValues() {
	}

	/**
	 * The single readable string behind a node, cleaned of markup and entities, or null when there
	 * isn't one. Whitespace is collapsed, so this is not the way to read a value whose line breaks
	 * matter - see {@link #markup}.
	 */
	public static String text(final JsonNode node) {
		return clean(rawText(node, 0));
	}

	/**
	 * As {@link #text} but keeping line breaks and any markup, for a caller that has to split the
	 * value on its own structure before cleaning it - a run of instructions in one string, say.
	 */
	public static String markup(final JsonNode node) {
		final String raw = rawText(node, 0);
		if (raw == null) {
			return null;
		}

		final String cleaned = HORIZONTAL_SPACE
						.matcher(INVISIBLE.matcher(unescapeEntities(raw)).replaceAll(" "))
						.replaceAll(" ")
						.trim();

		return cleaned.isEmpty() ? null : cleaned;
	}

	/** Every readable string in a node, whether it arrived as one value or a list of them. */
	public static List<String> texts(final JsonNode node) {
		if (isAbsent(node)) {
			return List.of();
		}

		if (node.isArray()) {
			final List<String> values = new ArrayList<>();
			for (final JsonNode child : node) {
				final String value = text(child);
				if (value != null) {
					values.add(value);
				}
			}
			return values;
		}

		final String value = text(node);
		return value == null ? List.of() : List.of(value);
	}

	/**
	 * The first readable string, or an empty string. Callers store this straight on the model, which
	 * has always held "" rather than null for a missing single value.
	 */
	public static String first(final JsonNode node) {
		return texts(node).stream().findFirst().orElse("");
	}

	/**
	 * A URL out of anything a site might hang one on: a bare string, an {@code ImageObject} with a
	 * {@code url} or a {@code contentUrl}, a list of either, or an {@code @id} pointing at a node
	 * elsewhere in the document.
	 */
	public static String url(final JsonNode node, final Map<String, JsonNode> idIndex) {
		return url(node, idIndex, 0);
	}

	/**
	 * Follows an {@code {"@id": "..."}} reference to the fuller node it names. A node that is already
	 * the richest one for its id, or whose id names nothing we saw, is returned untouched.
	 */
	public static JsonNode resolve(final JsonNode node, final Map<String, JsonNode> idIndex) {
		if (node == null || !node.isObject() || idIndex == null) {
			return node;
		}

		final JsonNode id = node.get("@id");
		if (id == null || !id.isTextual()) {
			return node;
		}

		final JsonNode target = idIndex.get(id.asText());
		return target == null || target == node || target.size() <= node.size() ? node : target;
	}

	/** The node itself, or its first element when it arrived wrapped in a list. */
	public static JsonNode unwrap(final JsonNode node) {
		if (node != null && node.isArray()) {
			for (final JsonNode child : node) {
				if (!isAbsent(child)) {
					return child;
				}
			}
			return null;
		}
		return isAbsent(node) ? null : node;
	}

	/**
	 * Whether a node carries the given schema.org type. {@code "@type"} is a single value or a list
	 * of them, and may be written plain, prefixed or as a full IRI - {@code Recipe},
	 * {@code schema:Recipe} and {@code http://schema.org/Recipe} all mean the same thing.
	 */
	public static boolean hasType(final JsonNode node, final String type) {
		if (node == null || !node.isObject()) {
			return false;
		}

		final JsonNode declared = node.has("@type") ? node.get("@type") : node.get("type");
		if (isAbsent(declared)) {
			return false;
		}

		if (declared.isArray()) {
			for (final JsonNode candidate : declared) {
				if (typeMatches(candidate.asText(), type)) {
					return true;
				}
			}
			return false;
		}

		return typeMatches(declared.asText(), type);
	}

	/** The node's first declared type, as written, or null. */
	public static String typeName(final JsonNode node) {
		if (node == null || !node.isObject()) {
			return null;
		}
		return text(node.has("@type") ? node.get("@type") : node.get("type"));
	}

	/**
	 * A time as an ISO-8601 duration. Sites are as likely to write "1 hr 30 mins", "1:30" or a bare
	 * number of minutes as they are "PT1H30M", so normalise what we recognise and hand back anything
	 * else untouched - a value we cannot read is still better shown than dropped.
	 */
	public static String duration(final JsonNode node) {
		final JsonNode unwrapped = unwrap(node);
		final JsonNode target = unwrapped != null && unwrapped.isObject()
						? firstPresent(unwrapped, "maxValue", "value", "@value")
						: unwrapped;

		final String raw = text(target);
		if (raw == null || ISO_DURATION.matcher(raw).matches()) {
			return raw;
		}

		final Long seconds = parseSeconds(raw);
		return seconds == null || seconds <= 0 ? raw : toIsoDuration(seconds);
	}

	private static JsonNode firstPresent(final JsonNode node, final String... keys) {
		for (final String key : keys) {
			final JsonNode child = node.get(key);
			if (!isAbsent(child)) {
				return child;
			}
		}
		return node;
	}

	private static Long parseSeconds(final String raw) {
		// "12-15 minutes" - take the upper bound, the way a cook reading the page would
		final Matcher range = RANGE.matcher(raw);
		final String value = range.find() ? raw.substring(range.end()) : raw;
		final String normalised = expandFractions(value).trim();

		final Matcher clock = CLOCK_DURATION.matcher(normalised);
		if (clock.matches()) {
			return Long.parseLong(clock.group(1)) * 3600 + Long.parseLong(clock.group(2)) * 60
							+ (clock.group(3) == null ? 0 : Long.parseLong(clock.group(3)));
		}

		// a lone number is minutes, which is how sites that omit the unit mean it
		if (BARE_NUMBER.matcher(normalised).matches()) {
			return Math.round(Double.parseDouble(normalised) * 60);
		}

		final Matcher parts = DURATION_PART.matcher(normalised);
		double seconds = 0;
		boolean matched = false;
		while (parts.find()) {
			matched = true;
			seconds += Double.parseDouble(parts.group(1)) * unitSeconds(parts.group(2));
		}

		return matched ? Math.round(seconds) : null;
	}

	private static long unitSeconds(final String unit) {
		final char first = Character.toLowerCase(unit.charAt(0));
		return switch (first) {
			case 'd' -> 86400L;
			case 'h' -> 3600L;
			case 's' -> 1L;
			default -> 60L;
		};
	}

	private static String expandFractions(final String value) {
		return value.replace("½", ".5").replace("¼", ".25").replace("¾", ".75")
						.replace("⅓", ".333").replace("⅔", ".667");
	}

	private static String toIsoDuration(final long totalSeconds) {
		final long days = totalSeconds / 86400;
		final long hours = totalSeconds % 86400 / 3600;
		final long minutes = totalSeconds % 3600 / 60;
		final long seconds = totalSeconds % 60;

		final StringBuilder iso = new StringBuilder("P");
		if (days > 0) {
			iso.append(days).append('D');
		}
		if (hours > 0 || minutes > 0 || seconds > 0) {
			iso.append('T');
			if (hours > 0) {
				iso.append(hours).append('H');
			}
			if (minutes > 0) {
				iso.append(minutes).append('M');
			}
			if (seconds > 0) {
				iso.append(seconds).append('S');
			}
		}
		return iso.toString();
	}

	private static String url(final JsonNode node, final Map<String, JsonNode> idIndex,
	                         final int depth) {
		if (isAbsent(node) || depth > MAX_DEPTH) {
			return null;
		}

		if (node.isValueNode()) {
			return normaliseUrl(node.asText());
		}

		if (node.isArray()) {
			for (final JsonNode child : node) {
				final String resolved = url(child, idIndex, depth + 1);
				if (resolved != null) {
					return resolved;
				}
			}
			return null;
		}

		final JsonNode target = resolve(node, idIndex);
		for (final String key : URL_KEYS) {
			final String resolved = url(target.get(key), idIndex, depth + 1);
			if (resolved != null) {
				return resolved;
			}
		}

		// A bare "@id" we could not resolve is only a URL if it isn't a fragment naming a graph node
		final JsonNode id = target.get("@id");
		if (id != null && id.isTextual() && !id.asText().contains("#")) {
			return normaliseUrl(id.asText());
		}

		return null;
	}

	private static String normaliseUrl(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		final String trimmed = value.trim();
		return trimmed.startsWith("//") ? "https:" + trimmed : trimmed;
	}

	private static String rawText(final JsonNode node, final int depth) {
		if (isAbsent(node) || depth > MAX_DEPTH) {
			return null;
		}

		if (node.isValueNode()) {
			return node.asText();
		}

		if (node.isArray()) {
			// a language tagged value arrives as one array element per language; take the first we can read
			for (final JsonNode child : node) {
				final String value = rawText(child, depth + 1);
				if (value != null && !value.isBlank()) {
					return value;
				}
			}
			return null;
		}

		for (final String key : VALUE_KEYS) {
			final String value = rawText(node.get(key), depth + 1);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}

		return null;
	}

	private static String clean(final String value) {
		if (value == null) {
			return null;
		}

		String cleaned = unescapeEntities(value);

		if (HTML_TAG.matcher(cleaned).find()) {
			cleaned = Jsoup.parse(cleaned).text();
		}

		cleaned = INVISIBLE.matcher(cleaned).replaceAll(" ");
		cleaned = WHITESPACE.matcher(cleaned).replaceAll(" ").trim();

		return cleaned.isEmpty() ? null : cleaned;
	}

	/**
	 * Entities are sometimes encoded twice ({@code &amp;#8217;}), so unescape until it settles.
	 */
	private static String unescapeEntities(final String value) {
		String current = value;
		for (int pass = 0; pass < 3; pass++) {
			final String next = Parser.unescapeEntities(current, false);
			if (next.equals(current)) {
				return current;
			}
			current = next;
		}
		return current;
	}

	private static boolean typeMatches(final String declared, final String wanted) {
		if (declared == null) {
			return false;
		}

		final String trimmed = declared.trim();
		final int cut = Math.max(trimmed.lastIndexOf('/'),
						Math.max(trimmed.lastIndexOf(':'), trimmed.lastIndexOf('#')));

		return trimmed.substring(cut + 1).equalsIgnoreCase(wanted);
	}

	static boolean isAbsent(final JsonNode node) {
		return node == null || node.isNull() || node.isMissingNode();
	}
}
