package to.charlie.foodPlanner.domain.extraction.manual.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class ExtractedRecipeStep implements ExtractedItem {

  private final String originalText;
  private final String tagName;
  private boolean possibleDuplicate;
}
