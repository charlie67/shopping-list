package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdGraphRoot {

	@JsonProperty("@graph")
	private List<JsonLdRecipe> graph;
}