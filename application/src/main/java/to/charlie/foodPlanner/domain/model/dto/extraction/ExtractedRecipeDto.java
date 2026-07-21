package to.charlie.foodPlanner.domain.model.dto.extraction;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtractedRecipeDto {

  private UUID id;

  private String url;

  private String name;

  private String imageUrl;

  private String description;

  private String dateModified;

  private String datePublished;

  private String keywords;

  private String cookTime;

  private String prepTime;

  private String totalTime;

  private String recipeCategory;

  private String recipeYield;

  private String calories;

  private String fatContent;

  private String saturatedFatContent;

  private String carbohydrateContent;

  private String sugarContent;

  private String fiberContent;

  private String proteinContent;

  private String sodiumContent;

  private String extractionMethod;

  private List<ExtractedRecipeStepsDto> instructions;

  private List<ExtractedIngredientDto> ingredients;
}
