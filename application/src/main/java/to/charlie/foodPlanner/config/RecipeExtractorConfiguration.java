package to.charlie.foodPlanner.config;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.ldExtraction.JsonLdExtractor;
import to.charlie.foodPlanner.domain.extraction.manual.ManualExtractor;
import to.charlie.foodPlanner.domain.extraction.microdata.MicrodataExtractor;

@RequiredArgsConstructor
@Component
public class RecipeExtractorConfiguration {

  private final JsonLdExtractor jsonLdExtractor;
  private final MicrodataExtractor microdataExtractor;
  private final ManualExtractor manualExtractor;

  @Bean
  @Qualifier("orderedRecipeExtractors")
  public List<RecipeExtractor> orderedRecipeExtractors() {
    // this is the ordered list that is traversed until a recipe is successfully extracted
    return List.of(jsonLdExtractor, microdataExtractor);
  }
}
