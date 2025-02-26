package to.charlie.foodPlanner.domain.model.dto.extraction;

import java.util.List;
import java.util.UUID;
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
public class ExtractedRecipeDto {

  private UUID id;

  private String url;

  private String name;

  private String imageUrl;

  private String description;

  private List<ExtractedRecipeStepsDto> instructions;

  private List<ExtractedIngredientDto> ingredients;
}
