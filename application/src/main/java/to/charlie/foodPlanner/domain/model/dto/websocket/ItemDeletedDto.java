package to.charlie.foodPlanner.domain.model.dto.websocket;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ItemDeletedDto extends DataDto {
  private UUID id;
}
