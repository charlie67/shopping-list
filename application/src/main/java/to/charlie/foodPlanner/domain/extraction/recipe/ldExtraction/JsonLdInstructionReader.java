package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads {@code recipeInstructions}, which is the least consistent property on a schema.org Recipe.
 * It arrives as a list of {@code HowToStep}s, a list of bare strings, one string with line breaks,
 * one string of HTML, a single step object, or {@code HowToSection}s that hold their real steps a
 * level down in {@code itemListElement}.
 */
@Component
public class JsonLdInstructionReader {

	private static final int MAX_DEPTH = 5;
	private static final String SECTION_TYPE = "HowToSection";
	private static final Pattern HTML_TAG = Pattern.compile("</?[a-zA-Z][^>]*>");
	private static final Pattern LINE_BREAK = Pattern.compile("\\R");

	/** The keys a section or list keeps its children under, in the order we prefer them. */
	private static final String[] CHILD_KEYS = {"itemListElement", "steps", "step"};

	public List<ExtractedRecipeInstruction> read(final JsonNode instructions,
	                                             final Map<String, JsonNode> idIndex) {

		if (JsonLdValues.isAbsent(instructions)) {
			return List.of();
		}

		// One string holds the whole method, so it is the only case we are entitled to split up
		if (instructions.isValueNode()) {
			return splitTextBlock(JsonLdValues.markup(instructions));
		}

		final List<ExtractedRecipeInstruction> steps = new ArrayList<>();
		collect(instructions, idIndex, steps, 0);
		return List.copyOf(steps);
	}

	private void collect(final JsonNode node, final Map<String, JsonNode> idIndex,
	                     final List<ExtractedRecipeInstruction> steps, final int depth) {

		if (JsonLdValues.isAbsent(node) || depth > MAX_DEPTH) {
			return;
		}

		if (node.isArray()) {
			for (final JsonNode child : node) {
				collect(child, idIndex, steps, depth + 1);
			}
			return;
		}

		if (node.isValueNode()) {
			addStep(steps, null, JsonLdValues.text(node));
			return;
		}

		final JsonNode step = JsonLdValues.resolve(node, idIndex);
		final boolean isSection = JsonLdValues.hasType(step, SECTION_TYPE);

		if (isSection) {
			// The heading is worth keeping - the model has no section of its own, so it rides along
			// as a step whose type says what it is.
			addStep(steps, SECTION_TYPE, JsonLdValues.text(step.get("name")));
		}

		final JsonNode children = children(step);

		// A section or a list is only a wrapper - the steps we want are the level below it
		if (isSection || JsonLdValues.hasType(step, "ItemList")) {
			collect(children, idIndex, steps, depth + 1);
			return;
		}

		// Plenty of sites carry the wording on "name" instead of "text"
		final String text = JsonLdValues.text(step.get("text"));
		final String wording = text != null ? text : JsonLdValues.text(step.get("name"));

		if (wording != null) {
			addStep(steps, JsonLdValues.typeName(step), wording);
			return;
		}

		// A step with no wording of its own still has its directions nested underneath
		collect(children, idIndex, steps, depth + 1);
	}

	private JsonNode children(final JsonNode step) {
		for (final String key : CHILD_KEYS) {
			final JsonNode child = step.get(key);
			if (!JsonLdValues.isAbsent(child)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * A single string of instructions is either HTML or plain text with line breaks. Split it the
	 * way the page meant it to read rather than storing the method as one paragraph.
	 */
	private List<ExtractedRecipeInstruction> splitTextBlock(final String block) {
		if (block == null) {
			return List.of();
		}

		final List<ExtractedRecipeInstruction> steps = new ArrayList<>();

		if (HTML_TAG.matcher(block).find()) {
			final Element body = Jsoup.parseBodyFragment(block).body();
			var elements = body.select("li");
			if (elements.isEmpty()) {
				elements = body.select("p");
			}

			if (!elements.isEmpty()) {
				elements.forEach(element -> addStep(steps, null, blankToNull(element.text())));
				return List.copyOf(steps);
			}
		}

		for (final String line : LINE_BREAK.split(block)) {
			addStep(steps, null, blankToNull(line));
		}

		return List.copyOf(steps);
	}

	private void addStep(final List<ExtractedRecipeInstruction> steps, final String type,
	                     final String text) {
		if (text == null || text.isBlank()) {
			return;
		}

		steps.add(ExtractedRecipeInstruction.builder().type(type).text(text.trim()).build());
	}

	private String blankToNull(final String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
