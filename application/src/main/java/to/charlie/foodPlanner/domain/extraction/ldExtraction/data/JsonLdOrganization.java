package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonLdOrganization {

  @JsonProperty("@type")
  private String type;
  private String name;
  private String url;
  private JsonLdImageObject logo;
}
