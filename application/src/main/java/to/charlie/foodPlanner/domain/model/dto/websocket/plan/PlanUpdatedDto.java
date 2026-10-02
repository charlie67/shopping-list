package to.charlie.foodPlanner.domain.model.dto.websocket.plan;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import to.charlie.foodPlanner.domain.model.dto.websocket.DataDto;

/**
 * One coarse event naming the weeks that changed, rather than an event per meal. Replaying placement
 * semantics - cascades, spare counts, which week a row belongs to - in the client would duplicate
 * logic the server owns, and a week is one cheap request.
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class PlanUpdatedDto extends DataDto {

  private List<LocalDate> weekStarts;
}
