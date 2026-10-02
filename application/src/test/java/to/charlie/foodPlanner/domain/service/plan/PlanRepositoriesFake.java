package to.charlie.foodPlanner.domain.service.plan;

import to.charlie.foodPlanner.domain.model.entity.plan.CookBatchEntity;
import to.charlie.foodPlanner.domain.model.entity.plan.PlannedMealEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.infrastructure.dal.repository.CookBatchRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.PlannedMealRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Backs the two plan repository mocks with a pair of in-memory lists.
 *
 * <p>The planner's rules are almost all about how batches and their meals relate - a leftover against
 * its cook night, a spare count against the rows that exist, a cascade that deletes some of them - so
 * per-call stubbing would say more about the stubs than the behaviour. These answers let a test set up
 * a plan, run a mutation and assert on what the week then looks like.
 */
final class PlanRepositoriesFake {

	private final List<CookBatchEntity> batches = new ArrayList<>();

	private final List<PlannedMealEntity> meals = new ArrayList<>();

	private PlanRepositoriesFake() {
	}

	static PlanRepositoriesFake wire(final CookBatchRepository cookBatchRepository,
	                                 final PlannedMealRepository plannedMealRepository) {
		final PlanRepositoriesFake fake = new PlanRepositoriesFake();

		when(cookBatchRepository.save(any())).thenAnswer(
						call -> fake.putBatch(call.getArgument(0)));
		when(cookBatchRepository.findById(any())).thenAnswer(call -> fake.batches.stream()
						.filter(batch -> batch.getId().equals(call.getArgument(0)))
						.findFirst());
		when(cookBatchRepository.findAllById(anyCollection())).thenAnswer(call -> {
			final Collection<UUID> ids = call.getArgument(0);
			return fake.batches.stream().filter(batch -> ids.contains(batch.getId())).toList();
		});
		when(cookBatchRepository.findQueued()).thenAnswer(call -> fake.batches.stream()
						.filter(batch -> fake.cookOf(batch.getId()).isEmpty())
						.toList());
		doAnswer(call -> fake.batches.remove(call.<CookBatchEntity>getArgument(0)))
						.when(cookBatchRepository).delete(any());

		when(plannedMealRepository.save(any())).thenAnswer(
						call -> fake.putMeal(call.getArgument(0)));
		when(plannedMealRepository.findById(any())).thenAnswer(call -> fake.meals.stream()
						.filter(meal -> meal.getId().equals(call.getArgument(0)))
						.findFirst());
		when(plannedMealRepository.findAllByBatchId(any())).thenAnswer(
						call -> fake.mealsOf(call.getArgument(0)));
		when(plannedMealRepository.findAllByBatchIdIn(anyCollection())).thenAnswer(call -> {
			final Collection<UUID> ids = call.getArgument(0);
			return fake.meals.stream().filter(meal -> ids.contains(meal.getBatch().getId())).toList();
		});
		when(plannedMealRepository.findAllByPlannedDateBetween(any(), any())).thenAnswer(call -> {
			final LocalDate from = call.getArgument(0);
			final LocalDate to = call.getArgument(1);
			return fake.meals.stream()
							.filter(meal -> meal.getPlannedDate() != null)
							.filter(meal -> !meal.getPlannedDate().isBefore(from)
											&& !meal.getPlannedDate().isAfter(to))
							.toList();
		});
		when(plannedMealRepository.findAllByPlacement(any())).thenAnswer(call -> fake.meals.stream()
						.filter(meal -> meal.getPlacement() == call.getArgument(0))
						.toList());
		when(plannedMealRepository.findAllByBatchIdInAndPlacement(anyCollection(), any()))
						.thenAnswer(call -> {
							final Collection<UUID> ids = call.getArgument(0);
							return fake.meals.stream()
											.filter(meal -> ids.contains(meal.getBatch().getId()))
											.filter(meal -> meal.getPlacement() == call.getArgument(1))
											.toList();
						});
		when(plannedMealRepository.findAllByPlacementAndPlannedDateGreaterThanEqual(any(), any()))
						.thenAnswer(call -> {
							final LocalDate from = call.getArgument(1);
							return fake.meals.stream()
											.filter(meal -> meal.getPlacement() == call.getArgument(0))
											.filter(meal -> meal.getPlannedDate() != null
															&& !meal.getPlannedDate().isBefore(from))
											.toList();
						});
		doAnswer(call -> fake.meals.remove(call.<PlannedMealEntity>getArgument(0)))
						.when(plannedMealRepository).delete(any());
		doAnswer(call -> fake.meals.removeAll(List.copyOf(call.<Collection<PlannedMealEntity>>getArgument(0))))
						.when(plannedMealRepository).deleteAll(anyCollection());

		return fake;
	}

	// ------------------------------------------------------------------ set-up

	CookBatchEntity givenBatch(final RecipeEntity recipe, final int mealsTotal, final int cookingFor) {
		return putBatch(CookBatchEntity.builder()
						.recipe(recipe)
						.mealsTotal(mealsTotal)
						.cookingFor(cookingFor)
						.build());
	}

	PlannedMealEntity givenMeal(final CookBatchEntity batch,
	                            final MealPlacement placement,
	                            final LocalDate plannedDate) {
		return putMeal(PlannedMealEntity.builder()
						.batch(batch)
						.placement(placement)
						.plannedDate(plannedDate)
						.build());
	}

	List<PlannedMealEntity> mealsOf(final UUID batchId) {
		return meals.stream().filter(meal -> meal.getBatch().getId().equals(batchId)).toList();
	}

	Optional<PlannedMealEntity> cookOf(final UUID batchId) {
		return mealsOf(batchId).stream()
						.filter(meal -> meal.getPlacement() == MealPlacement.COOK)
						.findFirst();
	}

	List<CookBatchEntity> allBatches() {
		return List.copyOf(batches);
	}

	List<PlannedMealEntity> allMeals() {
		return List.copyOf(meals);
	}

	private CookBatchEntity putBatch(final CookBatchEntity batch) {
		if (batch.getId() == null) {
			batch.setId(UUID.randomUUID());
		}
		if (!batches.contains(batch)) {
			batches.add(batch);
		}
		return batch;
	}

	private PlannedMealEntity putMeal(final PlannedMealEntity meal) {
		if (meal.getId() == null) {
			meal.setId(UUID.randomUUID());
		}
		if (!meals.contains(meal)) {
			meals.add(meal);
		}
		return meal;
	}
}
