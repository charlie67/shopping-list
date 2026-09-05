package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdRecipe {

	@JsonProperty("@context")
	private String context;
	@JsonProperty("@id")
	private String id;
	/** Either a single value or a list of them, hence untyped. */
	@JsonProperty("@type")
	private JsonNode type;
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
	private JsonNode keywords;
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