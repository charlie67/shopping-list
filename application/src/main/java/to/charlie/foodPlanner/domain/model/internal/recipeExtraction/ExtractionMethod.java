package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import java.util.Arrays;

public enum ExtractionMethod {
	MICRODATA("microdata"),
	JSON_LD("JSON-LD"),
	JUST_THE_RECIPE("JustTheRecipe");

	private final String name;

	ExtractionMethod(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ExtractionMethod fromName(final String name) {
		return Arrays.stream(values())
						.filter(method -> method.name.equals(name))
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Unknown extraction method: " + name));
	}
}
