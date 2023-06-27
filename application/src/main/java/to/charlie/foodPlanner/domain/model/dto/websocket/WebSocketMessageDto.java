package to.charlie.foodPlanner.domain.model.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class WebSocketMessageDto {

  private WebsocketUpdateType messageType;

  private DataDto data;
}
