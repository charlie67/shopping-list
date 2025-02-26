package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdHowToStep {

  @JsonProperty("@type")
  private String type;
  private String text;
}