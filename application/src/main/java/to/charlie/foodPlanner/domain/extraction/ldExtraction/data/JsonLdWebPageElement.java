package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
class JsonLdWebPageElement {

  @JsonAlias({"@type", "type"})
  private String type;
  private String isAccessibleForFree;
  private String cssSelector;
}