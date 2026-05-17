package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
class JsonLdWebPageElement {

	@JsonAlias({"@type", "type"})
	private String type;
	private String isAccessibleForFree;
	private String cssSelector;
}