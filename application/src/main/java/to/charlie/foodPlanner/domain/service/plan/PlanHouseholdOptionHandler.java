package to.charlie.foodPlanner.domain.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.plan.PlanUpdatedDto;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.domain.model.internal.plan.Household;
import to.charlie.foodPlanner.domain.service.OptionUpdateHandler;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;

import java.time.LocalDate;
import java.util.List;

import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.PLAN_UPDATED;

/**
 * The planner's response to the household setting changing: changing how many people you cook for
 * changes how many meals a pot makes, so the plan ahead of the household is reshaped to match.
 *
 * <p>This is the planner's business rather than the option store's, which is why it lives here and
 * reaches {@code OptionService} through {@link OptionUpdateHandler} instead of the other way round.
 *
 * <p>A value that is not a usable household size is still stored - the option is free text, and
 * another client may have written anything into it - but nothing is reshaped from it, and reads fall
 * back to the enum's default.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanHouseholdOptionHandler implements OptionUpdateHandler {

	private final PlanPortionService planPortionService;

	private final WebSocketService webSocketService;

	@Override
	public Option option() {
		return Option.PLAN_PORTION_SIZE;
	}

	/**
	 * The broadcast happens here rather than inside {@code applyHousehold} because that method is
	 * {@code @Transactional}: a broadcast on its last line would still run before the commit, and a
	 * phone that reacted by immediately re-reading the week would read the old state.
	 */
	@Override
	public void onOptionUpdated(final String value) {
		final Integer cookingFor = Household.parseOrNull(value);
		if (cookingFor == null) {
			log.info("Household option set to '{}', which is not a usable size, so the plan is left alone",
							value);
			return;
		}

		final List<LocalDate> weekStarts = planPortionService.applyHousehold(cookingFor);
		if (weekStarts.isEmpty()) {
			return;
		}

		webSocketService.sendMessageToAllClients(WebSocketMessageDto.builder()
						.data(PlanUpdatedDto.builder().weekStarts(weekStarts).build())
						.messageType(PLAN_UPDATED)
						.build());
	}
}
