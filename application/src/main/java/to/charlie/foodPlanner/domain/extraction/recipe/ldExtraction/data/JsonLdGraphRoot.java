package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdGraphRoot {

	/**
	 * Kept untyped so that a sibling node we don't care about (an author with an
	 * {@code "@type"} of {@code ["Person", "Organization"]}, say) can't fail the whole graph.
	 */
	@JsonProperty("@graph")
	private List<JsonNode> graph;
}
