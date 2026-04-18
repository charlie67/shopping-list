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
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonLdExtractor implements RecipeExtractor {

  private final ObjectMapper objectMapper;
  private final JsonLdExtractorConverter converter;

  public ExtractedRecipe extract(final Document document, final String url)
      throws RecipeExtractionFailed {
    final Elements elements = document.select("script[type=application/ld+json]");

    log.warn("{} JSON-LD scripts found for {}", elements.size(), document.title());

    for (final var element : elements) {
      final String jsonLd = element.data();

      try {
        final JsonLdGraphRoot graphRoot = objectMapper.readValue(jsonLd, JsonLdGraphRoot.class);

        if (graphRoot.getGraph() != null) {
          for (final JsonLdRecipe candidate : graphRoot.getGraph()) {
            if ("Recipe".equals(candidate.getType())) {
              return converter.convert(candidate);
            }
          }
        } else {
          throw new IllegalArgumentException("No graph found for JsonLd");
        }
      } catch (final JsonProcessingException | IllegalArgumentException e) {
        try {
          log.info("Unable to parse JSON-LD into a graph root", e);

          final JsonLdRecipe recipe = objectMapper.readValue(jsonLd, JsonLdRecipe.class);
          if (recipe != null && "Recipe".equals(recipe.getType())) {
            return converter.convert(recipe);
          }
        } catch (final JsonProcessingException e2) {
          log.error("Unable to parse JSON-LD into a recipe", e2);
        }
      }
    }

    throw new RecipeExtractionFailed("No recipe JSON-LD found");
  }
}
