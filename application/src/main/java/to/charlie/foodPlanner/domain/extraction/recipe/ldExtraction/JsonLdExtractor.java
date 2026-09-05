package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdGraphRoot;
import to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonLdExtractor implements RecipeExtractor {

	private final ObjectMapper objectMapper;
	private final JsonLdRecipeBuilder converter;

	public ExtractedRecipe extract(final Document document, final String url)
					throws RecipeExtractionFailed {
		final Elements elements = document.select("script[type=application/ld+json]");

		log.info("{} JSON-LD scripts found for {}", elements.size(), document.title());

		for (final var element : elements) {
			final String jsonLd = element.data();

			try {
				final JsonLdGraphRoot graphRoot = objectMapper.readValue(jsonLd, JsonLdGraphRoot.class);

				if (graphRoot.getGraph() != null) {
					for (final JsonNode candidate : graphRoot.getGraph()) {
						if (isRecipe(candidate)) {
							return converter.convert(objectMapper.treeToValue(candidate, JsonLdRecipe.class));
						}
					}
				} else {
					throw new IllegalArgumentException("No graph found for JsonLd");
				}
			} catch (final JsonProcessingException | IllegalArgumentException e) {
				try {
					log.info("Unable to parse JSON-LD into a graph root", e);

					final JsonNode root = objectMapper.readTree(jsonLd);
					if (isRecipe(root)) {
						return converter.convert(objectMapper.treeToValue(root, JsonLdRecipe.class));
					}
				} catch (final JsonProcessingException e2) {
					log.error("Unable to parse JSON-LD into a recipe", e2);
				}
			}
		}

		throw new RecipeExtractionFailed("No recipe JSON-LD found");
	}

	/**
	 * {@code "@type"} is either a single value or a list of them, so check for both.
	 */
	private boolean isRecipe(final JsonNode node) {
		final JsonNode type = node.get("@type");

		if (type == null) {
			return false;
		}

		if (type.isArray()) {
			for (final JsonNode candidate : type) {
				if ("Recipe".equals(candidate.asText())) {
					return true;
				}
			}
			return false;
		}

		return "Recipe".equals(type.asText());
	}

	@Override
	public ExtractionMethod getExtractionMethod() {
		return ExtractionMethod.JSON_LD;
	}
}
