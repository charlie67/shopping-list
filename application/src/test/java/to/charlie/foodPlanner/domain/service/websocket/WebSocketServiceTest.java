package to.charlie.foodPlanner.domain.service.websocket;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebSocketSession session;

    private WebSocketService webSocketService;

    @BeforeEach
    void setUp() {
        webSocketService = new WebSocketService(objectMapper);
    }

    @Test
    void handleNewConnection_whenSessionConnects_thenSessionIsTracked() {
        // given
        when(session.getId()).thenReturn("session-1");

        // when
        webSocketService.handleNewConnection(session);

        // then - subsequent sendMessageToAllClients will reach this session
        // (verified implicitly via sendMessageToAllClients test)
        assertThatNoException().isThrownBy(() -> webSocketService.handleNewConnection(session));
    }

    @Test
    void handleDisconnection_whenSessionIsOpen_thenClosesAndRemovesSession() throws IOException {
        // given
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        webSocketService.handleNewConnection(session);

        // when
        webSocketService.handleDisconnection(session);

        // then
        verify(session).close();
    }

    @Test
    void handleDisconnection_whenSessionIsAlreadyClosed_thenDoesNotCloseAgain() throws IOException {
        // given
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(false);
        webSocketService.handleNewConnection(session);

        // when
        webSocketService.handleDisconnection(session);

        // then
        verify(session, never()).close();
    }

    @Test
    void sendMessageToAllClients_whenSessionIsOpen_thenSendsMessage()
            throws IOException {
        // given
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"UPDATE\"}");
        webSocketService.handleNewConnection(session);

        WebSocketMessageDto message = WebSocketMessageDto.builder()
                .messageType(WebsocketUpdateType.SHOPPING_LIST_ITEM_CREATED)
                .build();

        // when
        webSocketService.sendMessageToAllClients(message);

        // then
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void sendMessageToAllClients_whenSessionIsClosed_thenDoesNotSendMessage()
            throws IOException {
        // given
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"UPDATE\"}");
        webSocketService.handleNewConnection(session);

        WebSocketMessageDto message = WebSocketMessageDto.builder()
                .messageType(WebsocketUpdateType.SHOPPING_LIST_ITEM_CREATED)
                .build();

        // when
        webSocketService.sendMessageToAllClients(message);

        // then
        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void sendMessageToAllClients_whenSerializationFails_thenDoesNotThrow()
            throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        WebSocketMessageDto message = WebSocketMessageDto.builder()
                .messageType(WebsocketUpdateType.SHOPPING_LIST_ITEM_CREATED)
                .build();

        // when / then
        assertThatNoException().isThrownBy(() -> webSocketService.sendMessageToAllClients(message));
    }

    @Test
    void handleDisconnection_whenSessionNotFound_thenDoesNotThrow() {
        // given
        when(session.getId()).thenReturn("unknown-session");
        when(session.isOpen()).thenReturn(false);

        // when / then - session was never added
        assertThatNoException().isThrownBy(() -> webSocketService.handleDisconnection(session));
    }
}
