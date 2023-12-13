package to.charlie.foodPlanner.domain.extraction.ldExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.ldExtraction.data.JsonLdRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonLdExtractor implements RecipeExtractor {
  private final ObjectMapper objectMapper;
  private final JsonLdExtractorConverter converter;

  public ExtractedRecipe extract(Document document) {
    Elements elements = document.select("script[type=application/ld+json]");

    if (elements.size() > 1) {
      log.warn("Multiple JSON-LD found for {} parsing first", document.title());
    }

    for (Element element : elements) {
      String jsonLd = element.data();
      final JsonLdRecipe recipe;

      try {
        recipe = objectMapper.readValue(jsonLd, JsonLdRecipe.class);
      } catch (JsonProcessingException e) {
        continue;
      }

      if (recipe.getType().equals("Recipe")) {
        return converter.convert(recipe);
      }
    }
    throw new IllegalArgumentException("No recipe JSON-LD found");
  }
}
