package to.charlie.foodPlanner.domain.service.plan;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.entity.plan.CookBatchEntity;
import to.charlie.foodPlanner.domain.model.entity.plan.PlannedMealEntity;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.infrastructure.dal.repository.CookBatchRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.PlannedMealRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Reshapes the plan when the household size changes.
 *
 * <p>Deliberately narrow in scope: this week's cook nights onwards, plus the queue. Past weeks and
 * the freezer keep the numbers they were planned with, because deleting a leftover you have already
 * eaten, or evaporating a portion that is physically in the freezer, is worse than a stale number.
 *
 * <p>Within that scope the {@code mealsTotal} snapshot is rewritten - that is the whole point of the
 * operation. "Snapshot" means it is not divided again on every read, not that it never changes. So a
 * shrinking household widens the pool (6 servings for one person is six meals where it was three)
 * and a growing one narrows it and sheds rows.
 *
 * <p>Depends only on the two plan repositories and a {@link Clock}, so there is no cycle with
 * {@link PlanService}, which reads the household through {@code OptionService} when planning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanPortionService {

	/**
	 * Shed a leftover before a frozen portion, and the newest leftover first: the further away a meal
	 * is, the less the household has committed to it. The cook night is never a candidate.
	 */
	private static final Comparator<PlannedMealEntity> SHED_ORDER =
					Comparator.<PlannedMealEntity, Integer>comparing(meal -> switch (meal.getPlacement()) {
						case LEFTOVER -> 0;
						case FROZEN -> 1;
						case COOK -> 2;
					}).thenComparing(PlannedMealEntity::getPlannedDate,
									Comparator.nullsLast(Comparator.reverseOrder()))
									.thenComparing(PlannedMealEntity::getId);

	private final CookBatchRepository cookBatchRepository;

	private final PlannedMealRepository plannedMealRepository;

	private final Clock clock;

	/**
	 * Applies a new household size to everything still ahead of the household.
	 *
	 * @return the Monday of every week the reshape touched, for the caller to broadcast once this has
	 *         committed. Empty when nothing changed.
	 */
	@Transactional
	public List<LocalDate> applyHousehold(final int cookingFor) {
		final LocalDate thisMonday = PlanService.mondayOf(LocalDate.now(clock));

		final Map<UUID, CookBatchEntity> inScope = new LinkedHashMap<>();
		plannedMealRepository
						.findAllByPlacementAndPlannedDateGreaterThanEqual(MealPlacement.COOK, thisMonday)
						.forEach(cook -> inScope.put(cook.getBatch().getId(), cook.getBatch()));
		cookBatchRepository.findQueued().forEach(batch -> inScope.put(batch.getId(), batch));

		final Set<LocalDate> touchedWeeks = new LinkedHashSet<>();

		for (final CookBatchEntity batch : inScope.values()) {
			// A batch whose recipe has no readable yield had its meal count typed in by hand. That is
			// not ours to overwrite.
			final Integer servings = batch.getRecipe().getServings();
			if (servings == null) {
				continue;
			}

			final int mealsTotal = PlanService.mealsTotalFor(servings, cookingFor);
			if (batch.getMealsTotal() == mealsTotal && batch.getCookingFor() == cookingFor) {
				continue;
			}

			batch.setMealsTotal(mealsTotal);
			batch.setCookingFor(cookingFor);
			cookBatchRepository.save(batch);

			final List<PlannedMealEntity> meals =
							new ArrayList<>(plannedMealRepository.findAllByBatchId(batch.getId()));
			meals.stream().map(PlannedMealEntity::getPlannedDate)
							.filter(Objects::nonNull)
							.map(PlanService::mondayOf)
							.forEach(touchedWeeks::add);

			// A growing household means fewer meals out of the same pot, so rows have to go.
			if (meals.size() > mealsTotal) {
				meals.sort(SHED_ORDER);
				final List<PlannedMealEntity> shed = meals.subList(0, meals.size() - mealsTotal).stream()
										.filter(meal -> meal.getPlacement() != MealPlacement.COOK)
										.toList();
				plannedMealRepository.deleteAll(shed);
				log.info("Household is now {}, so batch {} sheds {} of its {} meals",
								cookingFor, batch.getId(), shed.size(), meals.size());
			}

			// A queued batch has no dates at all, and the queue is on screen whichever week you are
			// looking at, so name the current week for it.
			touchedWeeks.add(thisMonday);
		}

		return List.copyOf(touchedWeeks);
	}
}
