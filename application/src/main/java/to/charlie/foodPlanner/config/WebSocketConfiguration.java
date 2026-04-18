package to.charlie.foodPlanner.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfiguration implements WebSocketConfigurer {

  private final WebSocketHandler webSocketHandler;

  @Override
  public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
    registry
        .addHandler(webSocketHandler, "/wsUpdate")
        .setAllowedOriginPatterns("*");
  }
}