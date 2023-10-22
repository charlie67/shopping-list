package to.charlie.foodPlanner.domain.extraction.manual;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.extraction.manual.ingredient.BaseIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.manual.ingredient.BasicsWithBabishIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.manual.recipe.BaseRecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.manual.recipe.BasicsWithBabishRecipeExtractor;

@Component
public class ExtractorFactory {
  final Map<String, ExtractorHolder> extractorMap;


  public ExtractorFactory() {
    extractorMap = Map.of("basicswithbabish.co", new ExtractorHolder(
            new BasicsWithBabishRecipeExtractor(), new BasicsWithBabishIngredientExtractor()));
  }

  private String getHost(String urlString) throws MalformedURLException {
    URL url = new URL(urlString);
    String domain = url.getHost().toLowerCase();
    return domain.startsWith("www.") ? url.getHost().substring(4) : domain;
  }

  public ExtractorHolder getExtractor(String urlString) throws MalformedURLException {
    String host = getHost(urlString);
    return extractorMap.getOrDefault(host, new ExtractorHolder(new BaseRecipeExtractor(),
            new BaseIngredientExtractor()));
  }
}
