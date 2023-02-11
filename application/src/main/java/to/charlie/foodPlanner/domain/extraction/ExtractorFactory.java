package to.charlie.foodPlanner.domain.extraction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import to.charlie.foodPlanner.domain.extraction.ingredient.BaseIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.ingredient.BasicsWithBabishIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.ingredient.BbcGoodFoodIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.ingredient.CustomiseableIngredientExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.BaseRecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.BasicsWithBabishRecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.BbcGoodFoodRecipeExtractor;
import to.charlie.foodPlanner.domain.extraction.recipe.CustomiseableRecipeExtractor;

public class ExtractorFactory {

  private static String getHost(String urlString) throws MalformedURLException {
    URL url = new URL(urlString);
    String domain = url.getHost().toLowerCase();
    return domain.startsWith("www.") ? url.getHost().substring(4) : domain;
  }

  public static ExtractorHolder getExtractor(String urlString) throws MalformedURLException {
    String host = getHost(urlString);

    if (host.equals("basicswithbabish.co")) {
      return new ExtractorHolder(
          new BasicsWithBabishRecipeExtractor(), new BasicsWithBabishIngredientExtractor());

    } else if (host.equals("bbcgoodfood.com")) {
      return new ExtractorHolder(
          new BbcGoodFoodRecipeExtractor(), new BbcGoodFoodIngredientExtractor());
    }

    return new ExtractorHolder(new BaseRecipeExtractor(), new BaseIngredientExtractor());
  }

  public static ExtractorHolder getExtractor(String recipeHeader, Set<String> recipeElement,
      String ingredientHeader, Set<String> ingredientElement) {
    return new ExtractorHolder(
        new CustomiseableRecipeExtractor(recipeHeader, recipeElement),
        new CustomiseableIngredientExtractor(ingredientHeader, ingredientElement));
  }
}
