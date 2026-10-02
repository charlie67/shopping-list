package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads a recipe out of the {@code application/ld+json} blocks on a page.
 *
 * <p>The shape of those blocks varies enormously: a recipe may sit at the top level, inside a
 * {@code @graph} (the WordPress default), inside a bare array of nodes, or nested under a page or
 * list node. So rather than expecting one layout, this walks every block collecting anything typed
 * as a Recipe, and indexes every {@code @id} it passes so that references between nodes resolve.
 */
@Component
@Slf4j
public class JsonLdExtractor implements RecipeExtractor {

	/**
	 * Deep enough for a recipe nested under a page inside a graph, shallow enough to stay cheap.
	 */
	private static final int MAX_DEPTH = 12;
	private static final int MAX_NODES = 5_000;

	private final ObjectMapper objectMapper;
	private final JsonLdRecipeBuilder converter;

	public JsonLdExtractor(@Qualifier("jsonLdObjectMapper") final ObjectMapper objectMapper,
	                       final JsonLdRecipeBuilder converter) {
		this.objectMapper = objectMapper;
		this.converter = converter;
	}

	public ExtractedRecipe extract(final Document document, final String url)
					throws RecipeExtractionFailed {
		// a "; charset=utf-8" suffix on the type is legal and does turn up
		final Elements elements = document.select("script[type^=application/ld+json]");

		log.info("{} JSON-LD scripts found for {}", elements.size(), document.title());

		final Map<String, JsonNode> idIndex = new HashMap<>();
		final List<JsonNode> candidates = new ArrayList<>();
		final Set<JsonNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());

		for (final var element : elements) {
			try {
				collect(objectMapper.readTree(element.data()), candidates, idIndex, visited, 0);
			} catch (final Exception e) {
				// one unreadable block is not the page's only chance of holding a recipe
				log.info("Skipping a JSON-LD block that could not be read for {}", url, e);
			}
		}

		candidates.sort(Comparator.comparingInt(JsonLdExtractor::weigh).reversed());

		for (final JsonNode candidate : candidates) {
			final ExtractedRecipe recipe = build(candidate, idIndex, url);

			if (recipe != null && isUsable(recipe)) {
				return recipe;
			}
		}

		throw new RecipeExtractionFailed(
						"No usable recipe JSON-LD found in " + elements.size() + " script(s), "
										+ candidates.size() + " of which were typed as a Recipe");
	}

	private ExtractedRecipe build(final JsonNode candidate, final Map<String, JsonNode> idIndex,
	                              final String url) {
		try {
			return converter.convert(objectMapper.treeToValue(candidate, JsonLdRecipe.class), idIndex);
		} catch (final Exception e) {
			log.info("Unable to read a JSON-LD Recipe node from {}", url, e);
			return null;
		}
	}

	/**
	 * A recipe with a name but nothing to cook is worse than no recipe at all - returning it would
	 * stop the extraction chain before microdata or JustTheRecipe ever saw the page.
	 */
	private boolean isUsable(final ExtractedRecipe recipe) {
		if (recipe.getName() == null || recipe.getName().isBlank()) {
			log.info("Discarding a JSON-LD Recipe node with no name");
			return false;
		}

		if (recipe.getExtractedRecipeIngredients().isEmpty()
						&& recipe.getExtractedRecipeInstructions().isEmpty()) {
			log.info("Discarding JSON-LD Recipe '{}' - no ingredients and no instructions",
							recipe.getName());
			return false;
		}

		return true;
	}

	private static int weigh(final JsonNode node) {
		return size(node.get("recipeIngredient")) + size(node.get("ingredients"))
						+ size(node.get("recipeInstructions")) + size(node.get("step"));
	}

	private static int size(final JsonNode node) {
		if (JsonLdValues.isAbsent(node)) {
			return 0;
		}
		return node.isContainerNode() ? node.size() : 1;
	}

	private void collect(final JsonNode node, final List<JsonNode> candidates,
	                     final Map<String, JsonNode> idIndex, final Set<JsonNode> visited,
	                     final int depth) {

		if (JsonLdValues.isAbsent(node) || node.isValueNode() || depth > MAX_DEPTH) {
			return;
		}

		// only containers are worth remembering; scalars are leaves and can never loop
		if (visited.size() > MAX_NODES || !visited.add(node)) {
			return;
		}

		if (node.isArray()) {
			for (final JsonNode child : node) {
				collect(child, candidates, idIndex, visited, depth + 1);
			}
			return;
		}

		if (!node.isObject()) {
			return;
		}

		final JsonNode id = node.get("@id");
		if (id != null && id.isTextual()) {
			// the same id can appear as a bare reference and as the full node; keep the fuller one
			idIndex.merge(id.asText(), node, (kept, found) -> kept.size() >= found.size() ? kept : found);
		}

		if (JsonLdValues.hasType(node, "Recipe")) {
			candidates.add(node);
		}

		for (final JsonNode child : node) {
			collect(child, candidates, idIndex, visited, depth + 1);
		}
	}

	@Override
	public ExtractionMethod getExtractionMethod() {
		return ExtractionMethod.JSON_LD;
	}
}
