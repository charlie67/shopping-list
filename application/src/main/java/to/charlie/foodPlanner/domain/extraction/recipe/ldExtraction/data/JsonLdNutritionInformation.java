package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdNutritionInformation {

	@JsonProperty("@type")
	private String type;
	private String calories;
	@JsonProperty("fatContent")
	private String fatContent;
	@JsonProperty("saturatedFatContent")
	private String saturatedFatContent;
	@JsonProperty("carbohydrateContent")
	private String carbohydrateContent;
	@JsonProperty("sugarContent")
	private String sugarContent;
	@JsonProperty("fiberContent")
	private String fiberContent;
	@JsonProperty("proteinContent")
	private String proteinContent;
	@JsonProperty("sodiumContent")
	private String sodiumContent;
	@JsonProperty("servingSize")
	private String servingSize; // todo use this
}