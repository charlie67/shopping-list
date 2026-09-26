package to.charlie.foodPlanner.domain.model.dto.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.charlie.foodPlanner.domain.model.internal.options.Option;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OptionDto {

	private Option name;

	private String value;
}
