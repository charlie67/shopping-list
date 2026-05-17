package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonLdImageObject {

	@JsonAlias({"@type", "type"})
	private String type;
	private int height;
	private String url;
	private int width;
}