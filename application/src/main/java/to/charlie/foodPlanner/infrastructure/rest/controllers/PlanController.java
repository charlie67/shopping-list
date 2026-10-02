package to.charlie.foodPlanner.infrastructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanWeekDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealUpdateDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.plan.PlanUpdatedDto;
import to.charlie.foodPlanner.domain.model.internal.plan.PlanMutation;
import to.charlie.foodPlanner.domain.service.plan.PlanService;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.function.Supplier;

import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.PLAN_UPDATED;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
@Slf4j
public class PlanController {

	private final PlanService planService;

	private final WebSocketService webSocketService;

	@GetMapping("/week/{weekStart}")
	public ResponseEntity<PlanWeekDto> getWeek(@PathVariable final String weekStart) {
		final LocalDate date;
		try {
			date = LocalDate.parse(weekStart);
		} catch (final DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(planService.getWeek(date));
	}

	@PostMapping
	public ResponseEntity<PlanWeekDto> plan(@RequestBody final PlanCreateDto createDto) {
		return respond(() -> planService.createBatch(createDto), HttpStatus.CREATED);
	}

	@PostMapping("/batch/{batchId}/meal")
	public ResponseEntity<PlanWeekDto> placeMeal(
					@PathVariable final UUID batchId,
					@RequestParam(required = false) final String week,
					@RequestBody final PlannedMealCreateDto createDto) {

		final LocalDate weekHint;
		try {
			weekHint = parseWeek(week);
		} catch (final DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
		return respond(() -> planService.placeMeal(batchId, createDto, weekHint), HttpStatus.OK);
	}

	@PatchMapping("/meal/{mealId}")
	public ResponseEntity<PlanWeekDto> updateMeal(
					@PathVariable final UUID mealId,
					@RequestParam(required = false) final String week,
					@RequestBody final PlannedMealUpdateDto updateDto) {

		final LocalDate weekHint;
		try {
			weekHint = parseWeek(week);
		} catch (final DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
		return respond(() -> planService.updateMeal(mealId, updateDto, weekHint), HttpStatus.OK);
	}

	@DeleteMapping("/meal/{mealId}")
	public ResponseEntity<PlanWeekDto> deleteMeal(
					@PathVariable final UUID mealId,
					@RequestParam(required = false) final String week) {

		final LocalDate weekHint;
		try {
			weekHint = parseWeek(week);
		} catch (final DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
		return respond(() -> planService.deleteMeal(mealId, weekHint), HttpStatus.OK);
	}

	@DeleteMapping("/batch/{batchId}")
	public ResponseEntity<PlanWeekDto> deleteBatch(
					@PathVariable final UUID batchId,
					@RequestParam(required = false) final String week) {

		final LocalDate weekHint;
		try {
			weekHint = parseWeek(week);
		} catch (final DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
		return respond(() -> planService.deleteBatch(batchId, weekHint), HttpStatus.OK);
	}

	private static LocalDate parseWeek(final String week) {
		return week == null || week.isBlank() ? null : LocalDate.parse(week);
	}

	/**
	 *
	 * The broadcast has to happen here rather than on the service's last line: {@code @Transactional}
	 * is proxy-based, so the commit happens as the annotated method returns to its caller.x A phone that
	 * reacted to {@code PLAN_UPDATED} by immediately re-reading the week on another connection would
	 * otherwise read the state from before the commit.
	 */
	private ResponseEntity<PlanWeekDto> respond(final Supplier<PlanMutation> mutation,
	                                            final HttpStatus success) {
		final PlanMutation result;
		try {
			result = mutation.get();
		} catch (final ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (final BadRequestException e) {
			log.info("Rejected plan change: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}

		webSocketService.sendMessageToAllClients(WebSocketMessageDto.builder()
						.data(PlanUpdatedDto.builder().weekStarts(result.affectedWeekStarts()).build())
						.messageType(PLAN_UPDATED)
						.build());

		return new ResponseEntity<>(result.week(), success);
	}
}
