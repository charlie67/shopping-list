package to.charlie.integrationTests.foodPlanner.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import to.charlie.integrationTests.foodPlanner.utilities.Ports;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Steps for asserting the server broadcasts shopping list changes over the {@code /wsUpdate}
 * WebSocket. A single client connection is opened per scenario and every text frame the server
 * pushes is buffered in a queue, so assertions can consume the broadcasts in the order they were
 * received.
 */
public class WebSocketSteps {

	private static final long MESSAGE_TIMEOUT_SECONDS = 5;

	private final BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();

	private WebSocketSession session;

	@SneakyThrows
	@Given("I am connected to the shopping list WebSocket")
	public void iAmConnectedToTheShoppingListWebSocket() {
		final URI uri = URI.create("ws://localhost:" + Ports.SPRING + "/wsUpdate");

		session = new StandardWebSocketClient()
						.execute(new TextWebSocketHandler() {
							@Override
							protected void handleTextMessage(final WebSocketSession session,
							                                 final TextMessage message) {
								receivedMessages.add(message.getPayload());
							}
						}, uri.toString())
						.get(MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	@SneakyThrows
	@Then("I should receive a WebSocket message with the following fields:")
	public void iShouldReceiveAWebSocketMessageWithTheFollowingFields(final DataTable table) {
		final String message = receivedMessages.poll(MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		if (message == null) {
			throw new AssertionError(
							"Expected to receive a WebSocket message within " + MESSAGE_TIMEOUT_SECONDS
											+ " seconds, but none arrived.");
		}

		HttpSteps.assertJsonFields(message, table);
	}

	@SneakyThrows
	@After
	public void closeWebSocket() {
		if (session != null && session.isOpen()) {
			session.close(CloseStatus.NORMAL);
		}
	}
}
