package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class JsonLdOrganization {
  @JsonProperty("@type")
  private String type;
  private String name;
  private String url;
  private JsonLdImageObject logo;
}
