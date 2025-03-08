package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdRecipe {

  @JsonProperty("@context")
  private String context;
  @JsonProperty("@id")
  private String id;
  @JsonProperty("@type")
  private String type;
  private String description;
  private JsonNode image;
  private JsonNode mainEntityOfPage;
  private String name;
  private String url;
  private JsonNode author;
  @JsonProperty("dateModified")
  private String dateModified;
  @JsonProperty("datePublished")
  private String datePublished;
  private String headline;
  private List<String> keywords;
  private JsonLdOrganization publisher;
  private String isAccessibleForFree;
  private JsonLdWebPageElement hasPart;
  private String cookTime;
  private JsonLdNutritionInformation nutrition;
  private String prepTime;
  private List<String> recipeCategory;
  private List<String> recipeCuisine; // todo map this all the way through
  @JsonProperty("recipeIngredient")
  private List<String> recipeIngredients;
  private JsonNode recipeInstructions;
  private List<String> recipeYield;
  private String totalTime;
}