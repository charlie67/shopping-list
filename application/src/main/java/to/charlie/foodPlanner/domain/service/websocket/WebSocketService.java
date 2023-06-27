package to.charlie.foodPlanner.domain.service.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

  private final Map<String, WebSocketSession> sessionMap = new HashMap<>();

  private final ObjectMapper objectMapper;

  public void handleNewConnection(final WebSocketSession session) {
    sessionMap.put(session.getId(), session);
  }

  public void handleDisconnection(final WebSocketSession session) {
    sessionMap.remove(session.getId());
  }

  public void sendMessageToAllClients(final WebSocketMessageDto messageDto) {
    final String message;
    try {
      message = objectMapper.writeValueAsString(messageDto);
      sessionMap.forEach((id, session) -> sendMessageToClient(message, session));
    } catch (final JsonProcessingException e) {
      log.error("Error sending message to client", e);
    }
  }

  private void sendMessageToClient(final String message, final WebSocketSession session) {
    try {
      session.sendMessage(new TextMessage(message));
    } catch (final IOException e) {
      log.error("Error sending message to client", e);
    }
  }
}
