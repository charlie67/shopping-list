package to.charlie.foodPlanner.domain.service.plan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.options.OptionDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanBatchDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanLeftoverDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanWeekDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealCreateDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealDto;
import to.charlie.foodPlanner.domain.model.dto.plan.PlannedMealUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.plan.CookBatchEntity;
import to.charlie.foodPlanner.domain.model.entity.plan.PlannedMealEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.domain.model.internal.plan.PlanMutation;
import to.charlie.foodPlanner.domain.service.OptionService;
import to.charlie.foodPlanner.infrastructure.dal.repository.CookBatchRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.PlannedMealRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
// The repository mocks are wired as an in-memory fake, so which of its answers a given test happens
// to reach varies. See PlanRepositoriesFake.
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanServiceTest {

	private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);
	private static final LocalDate TUESDAY = MONDAY.plusDays(1);
	private static final LocalDate WEDNESDAY = MONDAY.plusDays(2);
	private static final LocalDate THURSDAY = MONDAY.plusDays(3);
	private static final LocalDate SUNDAY = MONDAY.plusDays(6);
	private static final LocalDate NEXT_MONDAY = MONDAY.plusDays(7);

	@Mock
	private CookBatchRepository cookBatchRepository;

	@Mock
	private PlannedMealRepository plannedMealRepository;

	@Mock
	private RecipeRepository recipeRepository;

	@Mock
	private OptionService optionService;

	private PlanRepositoriesFake fake;
	private PlanService service;

	@BeforeEach
	void setUp() {
		fake = PlanRepositoriesFake.wire(cookBatchRepository, plannedMealRepository);
		givenHouseholdOf("2");

		// Wednesday of the week under test, so "this week" in the tests is the week of MONDAY.
		final Clock clock = Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC);
		service = new PlanService(cookBatchRepository, plannedMealRepository, recipeRepository,
						optionService, clock);
	}

	private void givenHouseholdOf(final String value) {
		when(optionService.getOption(Option.PLAN_PORTION_SIZE)).thenReturn(
						OptionDto.builder().name(Option.PLAN_PORTION_SIZE).value(value).build());
	}

	private RecipeEntity givenRecipe(final String name, final Integer servings) {
		final RecipeEntity recipe = RecipeEntity.builder()
						.id(UUID.randomUUID())
						.name(name)
						.recipeYield(servings == null ? null : String.valueOf(servings))
						.servings(servings)
						.imageUrl("https://example.com/" + name + ".jpg")
						.build();
		when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
		return recipe;
	}

	private static PlanCreateDto plan(final RecipeEntity recipe,
	                                  final LocalDate cookDate,
	                                  final PlanLeftoverDto... leftovers) {
		return PlanCreateDto.builder()
						.recipeId(recipe.getId())
						.cookDate(cookDate)
						.leftovers(List.of(leftovers))
						.build();
	}

	private static PlanLeftoverDto leftoverOn(final LocalDate date) {
		return PlanLeftoverDto.builder().placement(MealPlacement.LEFTOVER).plannedDate(date).build();
	}

	private static PlanLeftoverDto frozen() {
		return PlanLeftoverDto.builder().placement(MealPlacement.FROZEN).build();
	}

	private static PlanBatchDto onlyBatch(final PlanWeekDto week) {
		assertThat(week.getBatches()).hasSize(1);
		return week.getBatches().getFirst();
	}

	// --------------------------------------------------------------- the snapshot

	@Test
	void createBatch_whenNoMealsTotalGiven_thenDividesServingsByTheHousehold() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);

		// when
		final PlanWeekDto week = service.createBatch(plan(chilli, MONDAY)).week();

		// then
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(2);
		assertThat(onlyBatch(week).getCookingFor()).isEqualTo(2);
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
	}

	@Test
	void createBatch_whenTheRecipeServesFewerThanTheHousehold_thenMakesOneMeal() {
		// given
		final RecipeEntity soup = givenRecipe("Soup", 1);

		// when
		final PlanWeekDto week = service.createBatch(plan(soup, MONDAY)).week();

		// then
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(1);
		assertThat(onlyBatch(week).getSpareMeals()).isZero();
	}

	@Test
	void createBatch_whenTheRecipeHasNoReadableYield_thenTheClientsMealsTotalWins() {
		// given
		final RecipeEntity stew = givenRecipe("Stew", null);

		// when
		final PlanWeekDto week = service.createBatch(PlanCreateDto.builder()
						.recipeId(stew.getId())
						.cookDate(MONDAY)
						.mealsTotal(3)
						.leftovers(List.of(leftoverOn(WEDNESDAY)))
						.build()).week();

		// then
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(3);
		assertThat(onlyBatch(week).getServings()).isNull();
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
	}

	@Test
	void createBatch_whenCookingForIsGiven_thenItOverridesTheHousehold() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 6);

		// when
		final PlanWeekDto week = service.createBatch(PlanCreateDto.builder()
						.recipeId(chilli.getId())
						.cookDate(MONDAY)
						.cookingFor(3)
						.leftovers(List.of())
						.build()).week();

		// then
		assertThat(onlyBatch(week).getCookingFor()).isEqualTo(3);
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(2);
		// The week still echoes the household setting, not this batch's override.
		assertThat(week.getCookingFor()).isEqualTo(2);
	}

	@Test
	void createBatch_whenTheHouseholdOptionIsUnreadable_thenFallsBackToTheEnumDefault() {
		// given
		givenHouseholdOf("lots");
		final RecipeEntity chilli = givenRecipe("Chilli", 4);

		// when
		final PlanWeekDto week = service.createBatch(plan(chilli, MONDAY)).week();

		// then
		assertThat(week.getCookingFor()).isEqualTo(2);
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(2);
	}

	// ------------------------------------------------------------- the placements

	@Test
	void createBatch_whenALeftoverIsGiven_thenBooksTheCookNightAndTheLeftover() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);

		// when
		final PlanWeekDto week = service.createBatch(
						plan(chilli, MONDAY, leftoverOn(WEDNESDAY))).week();

		// then
		final PlanBatchDto batch = onlyBatch(week);
		assertThat(batch.getMeals()).extracting(PlannedMealDto::getPlacement)
						.containsExactly(MealPlacement.COOK, MealPlacement.LEFTOVER);
		assertThat(batch.getMeals()).extracting(PlannedMealDto::getPlannedDate)
						.containsExactly(MONDAY, WEDNESDAY);
		assertThat(batch.getSpareMeals()).isZero();
		assertThat(week.getWeekStart()).isEqualTo(MONDAY);
		assertThat(week.getWeekEnd()).isEqualTo(SUNDAY);
	}

	@Test
	void createBatch_whenMoreLeftoversThanThePotMakes_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final PlanCreateDto createDto =
						plan(chilli, MONDAY, leftoverOn(WEDNESDAY), leftoverOn(THURSDAY));

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("only makes 2 meals");
	}

	@Test
	void createBatch_whenALeftoverIsOnTheCookNight_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final PlanCreateDto createDto = plan(chilli, MONDAY, leftoverOn(MONDAY));

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("nothing to eat before");
	}

	@Test
	void createBatch_whenALeftoverIsBeforeTheCookNight_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final PlanCreateDto createDto = plan(chilli, WEDNESDAY, leftoverOn(TUESDAY));

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("nothing to eat before");
	}

	@Test
	void createBatch_whenQueuedWithLeftovers_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final PlanCreateDto createDto = plan(chilli, null, leftoverOn(WEDNESDAY));

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("no cook night");
	}

	@Test
	void createBatch_whenQueued_thenTheBatchIsInTheQueueWithNoSpareMeals() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 6);

		// when
		final PlanWeekDto week = service.createBatch(plan(chilli, null)).week();

		// then
		assertThat(week.getBatches()).isEmpty();
		assertThat(week.getQueue()).hasSize(1);
		final PlanBatchDto queued = week.getQueue().getFirst();
		assertThat(queued.getMealsTotal()).isEqualTo(3);
		// Its meals cannot be placed anywhere yet, so counting them into the pool would advertise
		// meals nothing can spend.
		assertThat(queued.getSpareMeals()).isZero();
		assertThat(queued.getMeals()).isEmpty();
	}

	@Test
	void createBatch_whenMealsTotalIsOutOfRange_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final PlanCreateDto createDto = PlanCreateDto.builder()
						.recipeId(chilli.getId())
						.cookDate(MONDAY)
						.mealsTotal(250)
						.leftovers(List.of())
						.build();

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("between 1 and 20");
	}

	@Test
	void createBatch_whenTheRecipeDoesNotExist_thenNotFound() {
		// given
		final UUID missing = UUID.randomUUID();
		when(recipeRepository.findById(missing)).thenReturn(Optional.empty());
		final PlanCreateDto createDto = PlanCreateDto.builder()
						.recipeId(missing)
						.cookDate(MONDAY)
						.leftovers(List.of())
						.build();

		// when / then
		assertThatThrownBy(() -> service.createBatch(createDto))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void createBatch_whenALeftoverIsFrozen_thenItIsDatelessAndShowsInTheFreezer() {
		// given
		final RecipeEntity ragu = givenRecipe("Ragu", 4);

		// when
		final PlanWeekDto week = service.createBatch(plan(ragu, MONDAY, frozen())).week();

		// then
		assertThat(week.getFreezer()).hasSize(1);
		assertThat(week.getFreezer().getFirst().getCookedOn()).isEqualTo(MONDAY);
		assertThat(week.getFreezer().getFirst().getRecipeName()).isEqualTo("Ragu");
		// The portion is deliberately reported twice: once in the freezer, once on its batch's row, so
		// the freezer panel renders without cross-referencing.
		assertThat(onlyBatch(week).getMeals()).extracting(PlannedMealDto::getPlacement)
						.containsExactly(MealPlacement.COOK, MealPlacement.FROZEN);
	}

	// --------------------------------------------------------------- placing spares

	@Test
	void placeMeal_whenASpareIsGivenADay_thenItBecomesALeftover() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 6);
		final CookBatchEntity batch = fake.givenBatch(chilli, 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when
		final PlanWeekDto week = service.placeMeal(batch.getId(),
						PlannedMealCreateDto.builder()
										.placement(MealPlacement.LEFTOVER)
										.plannedDate(WEDNESDAY)
										.build(), MONDAY).week();

		// then
		assertThat(onlyBatch(week).getMeals()).hasSize(2);
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
	}

	@Test
	void placeMeal_whenAQueuedBatchIsGivenADay_thenThatIsItsCookNight() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);

		// when
		final PlanWeekDto week = service.placeMeal(batch.getId(),
						PlannedMealCreateDto.builder()
										.placement(MealPlacement.COOK)
										.plannedDate(TUESDAY)
										.build(), null).week();

		// then
		assertThat(week.getQueue()).isEmpty();
		assertThat(onlyBatch(week).getMeals()).extracting(PlannedMealDto::getPlacement)
						.containsExactly(MealPlacement.COOK);
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
	}

	/**
	 * The success path for freezing a spare, which every other placeMeal test reached only by being
	 * rejected first. A frozen meal is dateless, and the week it touches was being collected with
	 * List.of - which rejects a null element - so this was a 500 every time.
	 */
	@Test
	void placeMeal_whenASpareIsFrozen_thenItIsDatelessAndShowsInTheFreezer() {
		// given
		final RecipeEntity ragu = givenRecipe("Ragu", 6);
		final CookBatchEntity batch = fake.givenBatch(ragu, 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when
		final PlanMutation mutation = service.placeMeal(batch.getId(),
						PlannedMealCreateDto.builder().placement(MealPlacement.FROZEN).build(), MONDAY);

		// then
		final PlanWeekDto week = mutation.week();
		assertThat(week.getFreezer()).hasSize(1);
		assertThat(week.getFreezer().getFirst().getCookedOn()).isEqualTo(MONDAY);
		assertThat(onlyBatch(week).getMeals()).extracting(PlannedMealDto::getPlacement)
						.containsExactly(MealPlacement.COOK, MealPlacement.FROZEN);
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
		// The frozen meal has no date of its own, so only the week asked for is named.
		assertThat(mutation.affectedWeekStarts()).containsExactly(MONDAY);
	}

	@Test
	void placeMeal_whenASpareIsFrozenWithNoWeekGiven_thenTheCurrentWeekIsAnswered() {
		// given
		final RecipeEntity ragu = givenRecipe("Ragu", 6);
		final CookBatchEntity batch = fake.givenBatch(ragu, 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when there is no date anywhere to fall back on, the clock decides
		final PlanWeekDto week = service.placeMeal(batch.getId(),
						PlannedMealCreateDto.builder().placement(MealPlacement.FROZEN).build(), null).week();

		// then
		assertThat(week.getWeekStart()).isEqualTo(MONDAY);
		assertThat(week.getFreezer()).hasSize(1);
	}

	@Test
	void placeMeal_whenTheBatchAlreadyHasACookNight_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealCreateDto createDto = PlannedMealCreateDto.builder()
						.placement(MealPlacement.COOK).plannedDate(TUESDAY).build();

		// when / then
		assertThatThrownBy(() -> service.placeMeal(batch.getId(), createDto, MONDAY))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("already cooked");
	}

	@Test
	void placeMeal_whenEveryMealAlreadyHasAHome_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);
		final PlannedMealCreateDto createDto = PlannedMealCreateDto.builder()
						.placement(MealPlacement.LEFTOVER).plannedDate(THURSDAY).build();

		// when / then
		assertThatThrownBy(() -> service.placeMeal(batch.getId(), createDto, MONDAY))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("already has a home");
	}

	@Test
	void placeMeal_whenTheBatchIsQueuedAndTheMealIsALeftover_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		final PlannedMealCreateDto createDto = PlannedMealCreateDto.builder()
						.placement(MealPlacement.LEFTOVER).plannedDate(WEDNESDAY).build();

		// when / then
		assertThatThrownBy(() -> service.placeMeal(batch.getId(), createDto, MONDAY))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("no cook night");
	}

	@Test
	void placeMeal_whenTheBatchDoesNotExist_thenNotFound() {
		// given
		final UUID missing = UUID.randomUUID();
		final PlannedMealCreateDto createDto = PlannedMealCreateDto.builder()
						.placement(MealPlacement.FROZEN).build();

		// when / then
		assertThatThrownBy(() -> service.placeMeal(missing, createDto, MONDAY))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	// ------------------------------------------------------------------- moving

	@Test
	void updateMeal_whenTheCookNightMovesPastItsLeftovers_thenTheyGoBackToThePool() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 6);
		final CookBatchEntity batch = fake.givenBatch(chilli, 3, 2);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, TUESDAY);
		final PlannedMealEntity keeper = fake.givenMeal(batch, MealPlacement.LEFTOVER, THURSDAY);

		// when
		final PlanWeekDto week = service.updateMeal(cook.getId(),
						PlannedMealUpdateDto.builder()
										.placement(MealPlacement.COOK)
										.plannedDate(WEDNESDAY)
										.build(), MONDAY).week();

		// then nothing is thrown away: the pot still makes three meals, one is now spare again
		final PlanBatchDto updated = onlyBatch(week);
		assertThat(updated.getMeals()).extracting(PlannedMealDto::getId)
						.containsExactly(cook.getId(), keeper.getId());
		assertThat(updated.getMealsTotal()).isEqualTo(3);
		assertThat(updated.getSpareMeals()).isEqualTo(1);
	}

	@Test
	void updateMeal_whenTheCookNightIsAskedToBecomeALeftover_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealUpdateDto updateDto = PlannedMealUpdateDto.builder()
						.placement(MealPlacement.LEFTOVER).plannedDate(WEDNESDAY).build();

		// when / then
		assertThatThrownBy(() -> service.updateMeal(cook.getId(), updateDto, MONDAY))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("cannot become a leftover");
	}

	@Test
	void updateMeal_whenALeftoverIsFrozen_thenItLosesItsDay() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealEntity leftover = fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		final PlanWeekDto week = service.updateMeal(leftover.getId(),
						PlannedMealUpdateDto.builder().placement(MealPlacement.FROZEN).build(), MONDAY).week();

		// then
		assertThat(week.getFreezer()).extracting(meal -> meal.getId())
						.containsExactly(leftover.getId());
		assertThat(onlyBatch(week).getMeals()).extracting(PlannedMealDto::getPlacement)
						.containsExactly(MealPlacement.COOK, MealPlacement.FROZEN);
		assertThat(onlyBatch(week).getSpareMeals()).isZero();
	}

	@Test
	void updateMeal_whenAFrozenMealComesOutOntoADay_thenItIsCheckedAgainstItsOwnCookNight() {
		// given a portion frozen from a pot cooked weeks ago
		final RecipeEntity ragu = givenRecipe("Ragu", 4);
		final CookBatchEntity batch = fake.givenBatch(ragu, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY.minusWeeks(3));
		final PlannedMealEntity portion = fake.givenMeal(batch, MealPlacement.FROZEN, null);

		// when
		final PlanWeekDto week = service.updateMeal(portion.getId(),
						PlannedMealUpdateDto.builder()
										.placement(MealPlacement.LEFTOVER)
										.plannedDate(TUESDAY)
										.build(), MONDAY).week();

		// then
		assertThat(week.getFreezer()).isEmpty();
		assertThat(onlyBatch(week).getMeals()).extracting(PlannedMealDto::getPlannedDate)
						.containsExactly(MONDAY.minusWeeks(3), TUESDAY);
	}

	@Test
	void updateMeal_whenALeftoverIsMovedBeforeItsCookNight_thenBadRequest() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, WEDNESDAY);
		final PlannedMealEntity leftover = fake.givenMeal(batch, MealPlacement.LEFTOVER, THURSDAY);
		final PlannedMealUpdateDto updateDto = PlannedMealUpdateDto.builder()
						.placement(MealPlacement.LEFTOVER).plannedDate(TUESDAY).build();

		// when / then
		assertThatThrownBy(() -> service.updateMeal(leftover.getId(), updateDto, MONDAY))
						.isInstanceOf(BadRequestException.class)
						.hasMessageContaining("nothing to eat before");
	}

	@Test
	void updateMeal_whenTheMealWasAlreadyDeletedByAnotherDevice_thenNotFound() {
		// given
		final PlannedMealUpdateDto updateDto = PlannedMealUpdateDto.builder()
						.placement(MealPlacement.LEFTOVER).plannedDate(WEDNESDAY).build();

		// when / then
		assertThatThrownBy(() -> service.updateMeal(UUID.randomUUID(), updateDto, MONDAY))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	// ------------------------------------------------------------------ deleting

	@Test
	void deleteMeal_whenALeftoverIsTakenOffItsDay_thenItGoesBackToThePool() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealEntity leftover = fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		final PlanWeekDto week = service.deleteMeal(leftover.getId(), MONDAY).week();

		// then
		assertThat(onlyBatch(week).getMealsTotal()).isEqualTo(2);
		assertThat(onlyBatch(week).getSpareMeals()).isEqualTo(1);
	}

	@Test
	void deleteMeal_whenTheCookNightIsRemoved_thenTheBatchIsRequeued() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 6);
		final CookBatchEntity batch = fake.givenBatch(chilli, 3, 2);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		final PlanWeekDto week = service.deleteMeal(cook.getId(), MONDAY).week();

		// then nothing can be eaten from a pot that is not cooked, so the whole batch goes back
		assertThat(week.getBatches()).isEmpty();
		assertThat(week.getQueue()).hasSize(1);
		assertThat(week.getQueue().getFirst().getMealsTotal()).isEqualTo(3);
		assertThat(week.getQueue().getFirst().getSpareMeals()).isZero();
		assertThat(fake.mealsOf(batch.getId())).isEmpty();
	}

	@Test
	void deleteBatch_whenTheBatchIsRemoved_thenItAndItsMealsAreGone() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		final PlanWeekDto week = service.deleteBatch(batch.getId(), MONDAY).week();

		// then
		assertThat(week.getBatches()).isEmpty();
		assertThat(week.getQueue()).isEmpty();
		assertThat(fake.allBatches()).isEmpty();
		assertThat(fake.allMeals()).isEmpty();
	}

	@Test
	void deleteBatch_whenTheBatchDoesNotExist_thenNotFound() {
		// given / when / then
		assertThatThrownBy(() -> service.deleteBatch(UUID.randomUUID(), MONDAY))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	// ------------------------------------------------------------------ the week

	@Test
	void getWeek_whenTheDateIsNotAMonday_thenItIsSnappedToItsMonday() {
		// given / when
		final PlanWeekDto week = service.getWeek(THURSDAY);

		// then
		assertThat(week.getWeekStart()).isEqualTo(MONDAY);
		assertThat(week.getWeekEnd()).isEqualTo(SUNDAY);
	}

	@Test
	void getWeek_whenABatchHasMealsOutsideTheWeek_thenTheyStillCountAgainstItsSpares() {
		// given a pot cooked on Sunday, with one of its leftovers eaten in the following week
		final RecipeEntity chilli = givenRecipe("Chilli", 6);
		final CookBatchEntity batch = fake.givenBatch(chilli, 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, SUNDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, NEXT_MONDAY);

		// when
		final PlanWeekDto week = service.getWeek(MONDAY);

		// then the batch reports the same two meals and one spare on either week's screen
		final PlanBatchDto batchDto = onlyBatch(week);
		assertThat(batchDto.getMeals()).extracting(PlannedMealDto::getPlannedDate)
						.containsExactly(SUNDAY, NEXT_MONDAY);
		assertThat(batchDto.getSpareMeals()).isEqualTo(1);
		assertThat(onlyBatch(service.getWeek(NEXT_MONDAY)).getSpareMeals()).isEqualTo(1);
	}

	@Test
	void getWeek_whenNothingIsPlanned_thenAnEmptyWeekEchoingTheHousehold() {
		// given
		givenHouseholdOf("4");

		// when
		final PlanWeekDto week = service.getWeek(MONDAY);

		// then
		assertThat(week.getBatches()).isEmpty();
		assertThat(week.getQueue()).isEmpty();
		assertThat(week.getFreezer()).isEmpty();
		assertThat(week.getCookingFor()).isEqualTo(4);
	}

	@Test
	void getWeek_whenABatchIsPlanned_thenItsRecipeIsDenormalisedOntoTheRow() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when
		final PlanBatchDto batchDto = onlyBatch(service.getWeek(MONDAY));

		// then the planner draws a chip without fetching a recipe that may be 200 rows deep
		assertThat(batchDto.getRecipeId()).isEqualTo(chilli.getId());
		assertThat(batchDto.getRecipeName()).isEqualTo("Chilli");
		assertThat(batchDto.getRecipeImageUrl()).isEqualTo("https://example.com/Chilli.jpg");
		assertThat(batchDto.getRecipeYield()).isEqualTo("4");
		assertThat(batchDto.getServings()).isEqualTo(4);
	}

	// ------------------------------------------------------------- what to broadcast

	@Test
	void updateMeal_whenALeftoverMovesIntoAnotherWeek_thenBothWeeksAreNamed() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealEntity leftover = fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		final PlanMutation mutation = service.updateMeal(leftover.getId(),
						PlannedMealUpdateDto.builder()
										.placement(MealPlacement.LEFTOVER)
										.plannedDate(NEXT_MONDAY)
										.build(), MONDAY);

		// then the week the client is not looking at has to be invalidated too
		assertThat(mutation.affectedWeekStarts()).containsExactlyInAnyOrder(MONDAY, NEXT_MONDAY);
		assertThat(mutation.week().getWeekStart()).isEqualTo(MONDAY);
	}

	@Test
	void placeMeal_whenNoWeekIsGiven_thenTheWeekOfTheMealIsAnswered() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);
		final CookBatchEntity batch = fake.givenBatch(chilli, 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when
		final PlanWeekDto week = service.placeMeal(batch.getId(),
						PlannedMealCreateDto.builder()
										.placement(MealPlacement.LEFTOVER)
										.plannedDate(NEXT_MONDAY.plusDays(1))
										.build(), null).week();

		// then
		assertThat(week.getWeekStart()).isEqualTo(NEXT_MONDAY);
	}

	@Test
	void createBatch_whenQueuedAndNoDateIsInvolved_thenTheCurrentWeekIsAnswered() {
		// given
		final RecipeEntity chilli = givenRecipe("Chilli", 4);

		// when
		final PlanWeekDto week = service.createBatch(plan(chilli, null)).week();

		// then the clock is fixed to the Wednesday of MONDAY's week
		assertThat(week.getWeekStart()).isEqualTo(MONDAY);
	}
}
