package to.charlie.foodPlanner.domain.service.plan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import to.charlie.foodPlanner.domain.model.entity.plan.CookBatchEntity;
import to.charlie.foodPlanner.domain.model.entity.plan.PlannedMealEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.infrastructure.dal.repository.CookBatchRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.PlannedMealRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The cutoff - "this week onwards" - is the whole point of this service, so every test runs against a
 * fixed clock. Without one the only way to test it would be to wait a week.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanPortionServiceTest {

	private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);
	private static final LocalDate TUESDAY = MONDAY.plusDays(1);
	private static final LocalDate WEDNESDAY = MONDAY.plusDays(2);
	private static final LocalDate THURSDAY = MONDAY.plusDays(3);
	private static final LocalDate FRIDAY = MONDAY.plusDays(4);
	private static final LocalDate LAST_MONDAY = MONDAY.minusWeeks(1);

	@Mock
	private CookBatchRepository cookBatchRepository;

	@Mock
	private PlannedMealRepository plannedMealRepository;

	private PlanRepositoriesFake fake;
	private PlanPortionService service;

	@BeforeEach
	void setUp() {
		fake = PlanRepositoriesFake.wire(cookBatchRepository, plannedMealRepository);

		// The Wednesday of MONDAY's week, so "this week" starts on MONDAY.
		final Clock clock = Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC);
		service = new PlanPortionService(cookBatchRepository, plannedMealRepository, clock);
	}

	private static RecipeEntity recipe(final String name, final Integer servings) {
		return RecipeEntity.builder()
						.id(UUID.randomUUID())
						.name(name)
						.recipeYield(servings == null ? null : String.valueOf(servings))
						.servings(servings)
						.build();
	}

	@Test
	void applyHousehold_whenItGrows_thenTheNewestLeftoverIsShedFirst() {
		// given a pot for six, feeding two, cooked on Monday with all three meals placed
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 6), 3, 2);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealEntity wednesday = fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);
		final PlannedMealEntity friday = fake.givenMeal(batch, MealPlacement.LEFTOVER, FRIDAY);

		// when the household grows to three, so the same pot now makes two meals
		service.applyHousehold(3);

		// then
		assertThat(batch.getMealsTotal()).isEqualTo(2);
		assertThat(batch.getCookingFor()).isEqualTo(3);
		assertThat(fake.mealsOf(batch.getId())).extracting(PlannedMealEntity::getId)
						.containsExactly(cook.getId(), wednesday.getId());
		assertThat(fake.allMeals()).doesNotContain(friday);
	}

	@Test
	void applyHousehold_whenItGrows_thenLeftoversGoBeforeFrozenPortions() {
		// given a pot for six with one leftover and one portion in the freezer
		final CookBatchEntity batch = fake.givenBatch(recipe("Ragu", 6), 3, 2);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		final PlannedMealEntity leftover = fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);
		final PlannedMealEntity portion = fake.givenMeal(batch, MealPlacement.FROZEN, null);

		// when
		service.applyHousehold(3);

		// then the portion that is physically in the freezer stays; the leftover goes
		assertThat(fake.mealsOf(batch.getId())).extracting(PlannedMealEntity::getId)
						.containsExactlyInAnyOrder(cook.getId(), portion.getId());
		assertThat(fake.allMeals()).doesNotContain(leftover);
	}

	@Test
	void applyHousehold_whenOnlyTheCookNightWouldBeLeft_thenItIsNeverShed() {
		// given a pot that now makes a single meal
		final CookBatchEntity batch = fake.givenBatch(recipe("Soup", 2), 2, 1);
		final PlannedMealEntity cook = fake.givenMeal(batch, MealPlacement.COOK, TUESDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, THURSDAY);

		// when
		service.applyHousehold(2);

		// then
		assertThat(batch.getMealsTotal()).isEqualTo(1);
		assertThat(fake.mealsOf(batch.getId())).extracting(PlannedMealEntity::getId)
						.containsExactly(cook.getId());
	}

	@Test
	void applyHousehold_whenItShrinks_thenThePoolWidensAndNothingIsDeleted() {
		// given a pot for six feeding two, so three meals, all of them placed
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 6), 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, FRIDAY);

		// when the household shrinks to one
		service.applyHousehold(1);

		// then six meals out of the same pot: three more to spend, and none of the rows touched
		assertThat(batch.getMealsTotal()).isEqualTo(6);
		assertThat(batch.getCookingFor()).isEqualTo(1);
		assertThat(fake.mealsOf(batch.getId())).hasSize(3);
	}

	@Test
	void applyHousehold_whenABatchWasCookedBeforeThisWeek_thenItIsLeftAlone() {
		// given last week's pot, its leftovers already eaten
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 6), 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, LAST_MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, LAST_MONDAY.plusDays(2));
		fake.givenMeal(batch, MealPlacement.LEFTOVER, LAST_MONDAY.plusDays(4));

		// when
		final List<LocalDate> touched = service.applyHousehold(3);

		// then deleting a leftover you have already eaten is worse than a stale number
		assertThat(batch.getMealsTotal()).isEqualTo(3);
		assertThat(batch.getCookingFor()).isEqualTo(2);
		assertThat(fake.mealsOf(batch.getId())).hasSize(3);
		assertThat(touched).isEmpty();
	}

	@Test
	void applyHousehold_whenTheRecipeHasNoReadableYield_thenTheHandTypedCountStands() {
		// given a batch whose meal count came from the client's own stepper
		final CookBatchEntity batch = fake.givenBatch(recipe("Mystery stew", null), 4, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, WEDNESDAY);

		// when
		service.applyHousehold(4);

		// then
		assertThat(batch.getMealsTotal()).isEqualTo(4);
		assertThat(batch.getCookingFor()).isEqualTo(2);
		assertThat(fake.mealsOf(batch.getId())).hasSize(2);
	}

	@Test
	void applyHousehold_whenABatchIsQueued_thenItIsReshapedAndTheCurrentWeekIsNamed() {
		// given a batch waiting in the queue, planned when the household was two
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 6), 3, 2);

		// when
		final List<LocalDate> touched = service.applyHousehold(3);

		// then
		assertThat(batch.getMealsTotal()).isEqualTo(2);
		assertThat(batch.getCookingFor()).isEqualTo(3);
		// The queue is on screen whichever week you are looking at, so the current week is invalidated.
		assertThat(touched).containsExactly(MONDAY);
	}

	@Test
	void applyHousehold_whenNothingChanges_thenNoWeeksAreNamed() {
		// given a batch already planned for this household
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 4), 2, 2);
		fake.givenMeal(batch, MealPlacement.COOK, MONDAY);

		// when
		final List<LocalDate> touched = service.applyHousehold(2);

		// then there is nothing to broadcast
		assertThat(touched).isEmpty();
	}

	@Test
	void applyHousehold_whenABatchSpansTwoWeeks_thenBothAreNamed() {
		// given a pot cooked on Friday with a leftover in the following week
		final CookBatchEntity batch = fake.givenBatch(recipe("Chilli", 6), 3, 2);
		fake.givenMeal(batch, MealPlacement.COOK, FRIDAY);
		fake.givenMeal(batch, MealPlacement.LEFTOVER, MONDAY.plusWeeks(1).plusDays(1));

		// when
		final List<LocalDate> touched = service.applyHousehold(3);

		// then
		assertThat(touched).containsExactlyInAnyOrder(MONDAY, MONDAY.plusWeeks(1));
	}
}
