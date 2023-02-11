package to.charlie.foodPlanner.domain.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TodoCreateDto {
  @NotEmpty(message = "Title is required")
  private String title;
}
