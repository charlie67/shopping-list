package to.charlie.foodPlanner.domain.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TodoUpdateDto {
  @NotEmpty(message = "Title is required")
  private String title;
}
