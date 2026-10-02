package to.charlie.foodPlanner.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import to.charlie.foodPlanner.config.ObjectMapperConfiguration;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanWeekDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealUpdateDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType;
import to.charlie.foodPlanner.domain.model.dto.websocket.plan.PlanUpdatedDto;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.domain.model.internal.plan.PlanMutation;
import to.charlie.foodPlanner.domain.service.plan.PlanService;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;
import to.charlie.foodPlanner.infrastructure.rest.controllers.PlanController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

	private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);
	private static final LocalDate THURSDAY = MONDAY.plusDays(3);

	@Mock
	private PlanService planService;

	@Mock
	private WebSocketService webSocketService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		// The application's own mapper, not the one standaloneSetup would wire for itself, which writes
		// LocalDate as a numeric array. Without this the date assertions below would be about MockMvc's
		// defaults rather than about what the app actually puts on the wire.
		objectMapper = new ObjectMapperConfiguration().objectMapper();
		mockMvc = MockMvcBuilders
						.standaloneSetup(new PlanController(planService, webSocketService))
						.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
						.build();
	}

	private static PlanWeekDto weekOf(final LocalDate monday) {
		return PlanWeekDto.builder()
						.weekStart(monday)
						.weekEnd(monday.plusDays(6))
						.cookingFor(2)
						.batches(List.of())
						.queue(List.of())
						.freezer(List.of())
						.build();
	}

	private static PlanMutation mutationOf(final LocalDate monday, final LocalDate... alsoTouched) {
		final List<LocalDate> weeks = new ArrayList<>();
		weeks.add(monday);
		weeks.addAll(List.of(alsoTouched));
		return new PlanMutation(weekOf(monday), List.copyOf(weeks));
	}

	private WebSocketMessageDto captureBroadcast() {
		final ArgumentCaptor<WebSocketMessageDto> captor =
						ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService).sendMessageToAllClients(captor.capture());
		return captor.getValue();
	}

	@Test
	void getWeek_whenAValidDate_thenReturnsTheWeek() throws Exception {
		// given
		when(planService.getWeek(MONDAY)).thenReturn(weekOf(MONDAY));

		// when / then
		mockMvc.perform(get("/plan/week/2026-09-21"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.weekStart").value("2026-09-21"))
						.andExpect(jsonPath("$.weekEnd").value("2026-09-27"))
						.andExpect(jsonPath("$.cookingFor").value(2));
	}

	@Test
	void getWeek_whenTheDateIsNotAMonday_thenItIsPassedThroughToBeSnapped() throws Exception {
		// given the date is normalised rather than validated, so the service is handed it as it arrives
		when(planService.getWeek(THURSDAY)).thenReturn(weekOf(MONDAY));

		// when / then
		mockMvc.perform(get("/plan/week/2026-09-24"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.weekStart").value("2026-09-21"));
	}

	@Test
	void getWeek_whenTheDateCannotBeRead_thenBadRequest() throws Exception {
		// given / when / then
		mockMvc.perform(get("/plan/week/not-a-date"))
						.andExpect(status().isBadRequest());
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}

	@Test
	void plan_whenValidBody_thenCreatedAndBroadcastsTheWeeksItTouched() throws Exception {
		// given
		final PlanCreateDto createDto = PlanCreateDto.builder()
						.recipeId(UUID.randomUUID())
						.cookDate(MONDAY)
						.leftovers(List.of())
						.build();
		when(planService.createBatch(any())).thenReturn(mutationOf(MONDAY, MONDAY.plusWeeks(1)));

		// when / then
		mockMvc.perform(post("/plan")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(createDto)))
						.andExpect(status().isCreated())
						.andExpect(jsonPath("$.weekStart").value("2026-09-21"));

		final WebSocketMessageDto broadcast = captureBroadcast();
		assertThat(broadcast.getMessageType()).isEqualTo(WebsocketUpdateType.PLAN_UPDATED);
		assertThat(((PlanUpdatedDto) broadcast.getData()).getWeekStarts())
						.containsExactly(MONDAY, MONDAY.plusWeeks(1));
	}

	@Test
	void plan_whenTheRecipeDoesNotExist_thenNotFoundAndNoBroadcast() throws Exception {
		// given
		when(planService.createBatch(any())).thenThrow(new ResourceNotFoundException("Recipe not found"));

		// when / then
		mockMvc.perform(post("/plan")
										.contentType(MediaType.APPLICATION_JSON)
										.content("{\"recipeId\":\"" + UUID.randomUUID() + "\",\"cookDate\":\"2026-09-21\"}"))
						.andExpect(status().isNotFound());
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}

	@Test
	void plan_whenTheServiceRejectsIt_thenBadRequestAndNoBroadcast() throws Exception {
		// given
		when(planService.createBatch(any())).thenThrow(new BadRequestException("Too many leftovers"));

		// when / then
		mockMvc.perform(post("/plan")
										.contentType(MediaType.APPLICATION_JSON)
										.content("{\"recipeId\":\"" + UUID.randomUUID() + "\",\"cookDate\":\"2026-09-21\"}"))
						.andExpect(status().isBadRequest());
		verify(webSocketService, never()).sendMessageToAllClients(any());
	}

	@Test
	void placeMeal_whenValidBody_thenReturnsTheWeek() throws Exception {
		// given
		final UUID batchId = UUID.randomUUID();
		when(planService.placeMeal(eq(batchId), any(), eq(MONDAY))).thenReturn(mutationOf(MONDAY));

		// when / then
		mockMvc.perform(post("/plan/batch/" + batchId + "/meal?week=2026-09-21")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(PlannedMealCreateDto.builder()
														.placement(MealPlacement.LEFTOVER)
														.plannedDate(THURSDAY)
														.build())))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.weekStart").value("2026-09-21"));
	}

	@Test
	void placeMeal_whenNoWeekIsGiven_thenTheServiceChoosesOne() throws Exception {
		// given
		final UUID batchId = UUID.randomUUID();
		when(planService.placeMeal(eq(batchId), any(), isNull())).thenReturn(mutationOf(MONDAY));

		// when / then
		mockMvc.perform(post("/plan/batch/" + batchId + "/meal")
										.contentType(MediaType.APPLICATION_JSON)
										.content("{\"placement\":\"FROZEN\"}"))
						.andExpect(status().isOk());
	}

	@Test
	void updateMeal_whenTheMealWasAlreadyDeleted_thenNotFound() throws Exception {
		// given the frontend treats this specifically: it refetches and lets its optimistic change
		// visibly undo itself, which it cannot do from a generic failure
		final UUID mealId = UUID.randomUUID();
		when(planService.updateMeal(eq(mealId), any(), eq(MONDAY)))
						.thenThrow(new ResourceNotFoundException("Meal not found"));

		// when / then
		mockMvc.perform(patch("/plan/meal/" + mealId + "?week=2026-09-21")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(PlannedMealUpdateDto.builder()
														.placement(MealPlacement.LEFTOVER)
														.plannedDate(THURSDAY)
														.build())))
						.andExpect(status().isNotFound());
	}

	@Test
	void updateMeal_whenTheWeekParamCannotBeRead_thenBadRequest() throws Exception {
		// given / when / then
		mockMvc.perform(patch("/plan/meal/" + UUID.randomUUID() + "?week=next-tuesday")
										.contentType(MediaType.APPLICATION_JSON)
										.content("{\"placement\":\"FROZEN\",\"plannedDate\":null}"))
						.andExpect(status().isBadRequest());
		verify(planService, never()).updateMeal(any(), any(), any());
	}

	@Test
	void deleteMeal_whenTheMealExists_thenReturnsTheWholeWeekRatherThanNoContent() throws Exception {
		// given every mutation answers with the week, because every mutation cascades
		final UUID mealId = UUID.randomUUID();
		when(planService.deleteMeal(mealId, MONDAY)).thenReturn(mutationOf(MONDAY));

		// when / then
		mockMvc.perform(delete("/plan/meal/" + mealId + "?week=2026-09-21"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.weekStart").value("2026-09-21"));
	}

	@Test
	void deleteBatch_whenTheBatchExists_thenReturnsTheWholeWeek() throws Exception {
		// given
		final UUID batchId = UUID.randomUUID();
		when(planService.deleteBatch(batchId, MONDAY)).thenReturn(mutationOf(MONDAY));

		// when / then
		mockMvc.perform(delete("/plan/batch/" + batchId + "?week=2026-09-21"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.queue").isArray());
	}

	@Test
	void deleteBatch_whenTheBatchDoesNotExist_thenNotFound() throws Exception {
		// given
		final UUID batchId = UUID.randomUUID();
		when(planService.deleteBatch(batchId, MONDAY))
						.thenThrow(new ResourceNotFoundException("Batch not found"));

		// when / then
		mockMvc.perform(delete("/plan/batch/" + batchId + "?week=2026-09-21"))
						.andExpect(status().isNotFound());
	}
}
