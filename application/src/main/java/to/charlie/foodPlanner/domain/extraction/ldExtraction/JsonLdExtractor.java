package to.charlie.foodPlanner.domain.extraction.ldExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.ldExtraction.data.JsonLdGraphRoot;
import to.charlie.foodPlanner.domain.extraction.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonLdExtractor implements RecipeExtractor {

  private final ObjectMapper objectMapper;
  private final JsonLdExtractorConverter converter;

  public ExtractedRecipe extract(final Document document) {
    final Elements elements = document.select("script[type=application/ld+json]");

    if (elements.size() > 1) {
      log.warn("Multiple JSON-LD found for {} parsing first", document.title());
    }

    for (final var element : elements) {
      final String jsonLd = element.data();

      JsonLdRecipe recipe = null;
      try {
        final JsonLdGraphRoot graphRoot = objectMapper.readValue(jsonLd, JsonLdGraphRoot.class);

        // first try any graph tags
        if (graphRoot.getGraph() != null) {
          for (final JsonLdRecipe candidate : graphRoot.getGraph()) {
            if ("Recipe".equals(candidate.getType())) {
              return converter.convert(candidate);
            }
          }
        }
      } catch (final JsonProcessingException e) {
        try {
          recipe = objectMapper.readValue(jsonLd, JsonLdRecipe.class);
          if (recipe != null && "Recipe".equals(recipe.getType())) {
            return converter.convert(recipe);
          }
        } catch (final JsonProcessingException ignored) {
        }
      }

    }
    throw new IllegalArgumentException("No recipe JSON-LD found");
  }
}
