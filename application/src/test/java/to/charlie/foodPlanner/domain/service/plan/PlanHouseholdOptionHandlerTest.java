package to.charlie.foodPlanner.domain.service.plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType;
import to.charlie.foodPlanner.domain.model.dto.websocket.plan.PlanUpdatedDto;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanHouseholdOptionHandlerTest {

	private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);

	@Mock
	private PlanPortionService planPortionService;

	@Mock
	private WebSocketService webSocketService;

	@InjectMocks
	private PlanHouseholdOptionHandler handler;

	@Test
	void option_isTheHouseholdSize() {
		// given / when / then
		assertThat(handler.option()).isEqualTo(Option.PLAN_PORTION_SIZE);
	}

	@Test
	void onOptionUpdated_whenAUsableSize_thenReshapesThePlanAndBroadcastsTheWeeks() {
		// given
		when(planPortionService.applyHousehold(4)).thenReturn(List.of(MONDAY, MONDAY.plusWeeks(1)));

		// when
		handler.onOptionUpdated("4");

		// then
		verify(planPortionService).applyHousehold(4);
		final ArgumentCaptor<WebSocketMessageDto> captor =
						ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService).sendMessageToAllClients(captor.capture());
		assertThat(captor.getValue().getMessageType()).isEqualTo(WebsocketUpdateType.PLAN_UPDATED);
		assertThat(((PlanUpdatedDto) captor.getValue().getData()).getWeekStarts())
						.containsExactly(MONDAY, MONDAY.plusWeeks(1));
	}

	@Test
	void onOptionUpdated_whenNothingWasReshaped_thenNoBroadcast() {
		// given
		when(planPortionService.applyHousehold(2)).thenReturn(List.of());

		// when
		handler.onOptionUpdated("2");

		// then there is nothing for another device to invalidate
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}

	@Test
	void onOptionUpdated_whenTheValueIsUnreadable_thenThePlanIsLeftAlone() {
		// given the option is free text and another client may have written anything into it
		// when
		handler.onOptionUpdated("lots");

		// then
		verify(planPortionService, never()).applyHousehold(anyInt());
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}

	@Test
	void onOptionUpdated_whenTheValueIsOutOfRange_thenThePlanIsLeftAlone() {
		// given a size nobody cooks for, which the planner refuses to reshape around
		// when
		handler.onOptionUpdated("99");

		// then
		verify(planPortionService, never()).applyHousehold(anyInt());
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}
}
