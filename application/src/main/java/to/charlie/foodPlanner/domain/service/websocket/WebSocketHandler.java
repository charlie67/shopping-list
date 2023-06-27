package to.charlie.foodPlanner.domain.service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

  private final WebSocketService webSocketService;

  public void sendMessageToClient(final WebSocketSession session, final String message)
      throws Exception {
    session.sendMessage(new TextMessage(message));
  }

  @Override
  public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
    super.afterConnectionEstablished(session);
    webSocketService.handleNewConnection(session);
  }

  @Override
  public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status)
      throws Exception {
    super.afterConnectionClosed(session, status);
    webSocketService.handleDisconnection(session);
  }
}
