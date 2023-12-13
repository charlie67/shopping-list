package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import lombok.Data;

@Data
class JsonLdWebPageElement {
  private String type;
  private String isAccessibleForFree;
  private String cssSelector;
}