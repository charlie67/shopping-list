package to.charlie.foodPlanner.domain.model.internal.plan;

import to.charlie.foodPlanner.domain.model.internal.options.Option;

public final class Household {

	public static final int MIN = 1;

	public static final int MAX = 8;

	private Household() {
	}

	public static int parse(final String value) {
		final Integer parsed = parseOrNull(value);
		if (parsed == null) {
			return Integer.parseInt(Option.PLAN_PORTION_SIZE.getDefaultValue());
		}
		return Math.min(MAX, Math.max(MIN, parsed));
	}

	public static Integer parseOrNull(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final int parsed;
		try {
			parsed = Integer.parseInt(value.trim());
		} catch (final NumberFormatException e) {
			return null;
		}
		return parsed >= MIN && parsed <= MAX ? parsed : null;
	}
}
