package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractedRecipe {

  private String description;
  private String name;
  private String url;
  private String dateModified;
  private String datePublished;
  private List<String> keywords;
  private String cookTime;
  private String prepTime;
  private String totalTime;
  private String recipeCategory;
  private String recipeYield;
  private List<ExtractedRecipeIngredient> extractedRecipeIngredients;
  private List<ExtractedRecipeInstruction> extractedRecipeInstructions;
  private String calories;
  private String fatContent;
  private String saturatedFatContent;
  private String carbohydrateContent;
  private String sugarContent;
  private String fiberContent;
  private String proteinContent;
  private String sodiumContent;
  private String extractionMethod;
  private String imageUrl;
}
