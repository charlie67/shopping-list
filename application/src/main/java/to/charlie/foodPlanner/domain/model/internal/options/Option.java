package to.charlie.foodPlanner.domain.model.internal.options;

public enum Option {
	PLAN_PORTION_SIZE("2");

	private final String defaultValue;

	Option(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
