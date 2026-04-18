package to.charlie.foodPlanner.domain.model.dto.extraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtractedRecipeStepsDto {

  private String text;

  private String type;

  private int stepCount;

  private boolean possibleDuplicate;
}
