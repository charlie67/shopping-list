package to.charlie.foodPlanner.domain.extraction.ldExtraction.data;

import lombok.Data;

@Data
class JsonLdImageObject {
  private String type;
  private int height;
  private String url;
  private int width;
}