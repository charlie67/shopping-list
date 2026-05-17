package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import lombok.Data;

@Data
class JsonLdPerson {
	private String type;
	private String name;
	private String url;
}