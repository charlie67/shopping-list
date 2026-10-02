package to.charlie.foodPlanner.domain.service.plan;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.plan.FrozenMealDto;
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
import to.charlie.foodPlanner.domain.model.internal.plan.Household;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;
import to.charlie.foodPlanner.domain.model.internal.plan.PlanMutation;
import to.charlie.foodPlanner.domain.service.OptionService;
import to.charlie.foodPlanner.infrastructure.dal.repository.CookBatchRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.PlannedMealRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeRepository;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

	public static final int MIN_MEALS_TOTAL = 1;

	public static final int MAX_MEALS_TOTAL = 20;

	/**
	 * Cook first, then leftovers in the order they will be eaten, then the freezer.
	 */
	private static final Comparator<PlannedMealEntity> MEAL_ORDER =
					Comparator.<PlannedMealEntity, Integer>comparing(meal -> meal.getPlacement().ordinal())
									.thenComparing(PlannedMealEntity::getPlannedDate,
													Comparator.nullsLast(Comparator.naturalOrder()))
									.thenComparing(PlannedMealEntity::getId);

	private final CookBatchRepository cookBatchRepository;

	private final PlannedMealRepository plannedMealRepository;

	private final RecipeRepository recipeRepository;

	private final OptionService optionService;

	private final Clock clock;

	/**
	 * Any date in the week, snapped back to its Monday.
	 */
	public static LocalDate mondayOf(final LocalDate date) {
		return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

	/**
	 * The week read. The incoming date is normalised rather than validated - a client that is a day
	 * out across a clock change gets a coherent answer rather than a 400.
	 */
	public PlanWeekDto getWeek(final LocalDate anyDateInWeek) {
		return assembleWeek(mondayOf(anyDateInWeek));
	}

	@Transactional
	public PlanMutation createBatch(final PlanCreateDto createDto) {
		if (createDto.getRecipeId() == null) {
			throw new BadRequestException("A plan needs a recipe");
		}

		final RecipeEntity recipe = recipeRepository.findById(createDto.getRecipeId())
						.orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

		final int cookingFor = createDto.getCookingFor() == null
						? householdSize()
						: requireHousehold(createDto.getCookingFor());

		final int mealsTotal = createDto.getMealsTotal() == null
						? mealsTotalFor(recipe.getServings(), cookingFor)
						: createDto.getMealsTotal();

		if (mealsTotal < MIN_MEALS_TOTAL || mealsTotal > MAX_MEALS_TOTAL) {
			throw new BadRequestException(
							"A batch has to make between " + MIN_MEALS_TOTAL + " and " + MAX_MEALS_TOTAL + " meals");
		}

		final LocalDate cookDate = createDto.getCookDate();
		final List<PlanLeftoverDto> leftovers = createDto.getLeftovers() == null
						? List.of()
						: createDto.getLeftovers();

		if (cookDate == null && !leftovers.isEmpty()) {
			throw new BadRequestException("You cannot eat or freeze from a batch with no cook night");
		}
		if (leftovers.size() > mealsTotal - 1) {
			throw new BadRequestException(
							"That batch only makes " + mealsTotal + " meals, so it has "
											+ (mealsTotal - 1) + " to place beyond the cook night");
		}
		leftovers.forEach(leftover ->
						requirePlaceable(leftover.getPlacement(), leftover.getPlannedDate(), cookDate));

		final CookBatchEntity batch = cookBatchRepository.save(CookBatchEntity.builder()
						.recipe(recipe)
						.mealsTotal(mealsTotal)
						.cookingFor(cookingFor)
						.build());

		if (cookDate != null) {
			plannedMealRepository.save(meal(batch, MealPlacement.COOK, cookDate));
			leftovers.forEach(leftover -> plannedMealRepository.save(
							meal(batch, leftover.getPlacement(), leftover.getPlannedDate())));
		}

		log.info("Planned recipe {} as a batch of {} meals, cooking {}",
						recipe.getId(), mealsTotal, cookDate == null ? "unscheduled" : cookDate);

		final List<LocalDate> dates = new ArrayList<>();
		dates.add(cookDate);
		leftovers.forEach(leftover -> dates.add(leftover.getPlannedDate()));

		return mutation(resolveWeek(null, cookDate), dates);
	}

	/**
	 * Gives one of a batch's spare meals a home. A spare has no row until it is placed.
	 */
	@Transactional
	public PlanMutation placeMeal(final UUID batchId,
	                              final PlannedMealCreateDto createDto,
	                              final LocalDate weekHint) {
		final CookBatchEntity batch = cookBatchRepository.findById(batchId)
						.orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

		final List<PlannedMealEntity> existing = plannedMealRepository.findAllByBatchId(batchId);
		if (existing.size() >= batch.getMealsTotal()) {
			throw new BadRequestException("Every meal that batch makes already has a home");
		}

		final Optional<PlannedMealEntity> cook = cookOf(existing);
		final MealPlacement placement = createDto.getPlacement();
		final LocalDate plannedDate = createDto.getPlannedDate();

		if (placement == MealPlacement.COOK) {
			// Placing the first meal of a queued batch is choosing its cook night.
			if (cook.isPresent()) {
				throw new BadRequestException("That batch is already cooked on "
								+ cook.get().getPlannedDate());
			}
			if (plannedDate == null) {
				throw new BadRequestException("A cook night needs a day");
			}
		} else {
			requirePlaceable(placement, plannedDate,
							cook.map(PlannedMealEntity::getPlannedDate).orElse(null));
		}

		plannedMealRepository.save(meal(batch, placement, plannedDate));

		// singletonList, not List.of: a frozen meal is deliberately dateless, and List.of rejects a
		// null element outright. mutation() filters the nulls out itself.
		return mutation(resolveWeek(weekHint, plannedDate), Collections.singletonList(plannedDate));
	}

	/**
	 * Covers every placement gesture: a chip moved to another day, the freezer to a day, a day to the
	 * freezer, and moving a cook night. A meal coming out of the freezer needs no exemption from the
	 * date rule - it is checked against its own batch's cook date, which for a genuinely old portion
	 * is weeks in the past and therefore always passes.
	 */
	@Transactional
	public PlanMutation updateMeal(final UUID mealId,
	                               final PlannedMealUpdateDto updateDto,
	                               final LocalDate weekHint) {
		final PlannedMealEntity mealToUpdate = plannedMealRepository.findById(mealId)
						.orElseThrow(() -> new ResourceNotFoundException("Meal not found"));

		final CookBatchEntity batch = mealToUpdate.getBatch();
		final List<PlannedMealEntity> siblings = plannedMealRepository.findAllByBatchId(batch.getId());
		final boolean isTheCook = mealToUpdate.getPlacement() == MealPlacement.COOK;

		final List<LocalDate> touched = new ArrayList<>();
		touched.add(mealToUpdate.getPlannedDate());
		touched.add(updateDto.getPlannedDate());

		if (isTheCook) {
			if (updateDto.getPlacement() != MealPlacement.COOK) {
				throw new BadRequestException(
								"A batch's cook night cannot become a leftover. Take it off the day instead");
			}
			if (updateDto.getPlannedDate() == null) {
				throw new BadRequestException("A cook night needs a day");
			}

			mealToUpdate.setPlannedDate(updateDto.getPlannedDate());
			plannedMealRepository.save(mealToUpdate);

			// Leftovers that now fall before the night they came from go back to the pool as spares.
			// Nothing is thrown away: the batch still makes the same number of meals.
			stranded(siblings, updateDto.getPlannedDate()).forEach(leftover -> {
				touched.add(leftover.getPlannedDate());
				plannedMealRepository.delete(leftover);
			});
		} else {
			if (updateDto.getPlacement() == MealPlacement.COOK) {
				throw new BadRequestException("That batch already has a cook night");
			}
			requirePlaceable(updateDto.getPlacement(), updateDto.getPlannedDate(),
							cookOf(siblings).map(PlannedMealEntity::getPlannedDate).orElse(null));

			mealToUpdate.setPlacement(updateDto.getPlacement());
			mealToUpdate.setPlannedDate(updateDto.getPlannedDate());
			plannedMealRepository.save(mealToUpdate);
		}

		return mutation(resolveWeek(weekHint, updateDto.getPlannedDate()), touched);
	}

	/**
	 * Takes a meal off its day, handing it back to the pool as a spare. Deleting the cook night
	 * requeues the whole batch, since nothing can be eaten or frozen from a pot that is not cooked.
	 */
	@Transactional
	public PlanMutation deleteMeal(final UUID mealId, final LocalDate weekHint) {
		final PlannedMealEntity mealToDelete = plannedMealRepository.findById(mealId)
						.orElseThrow(() -> new ResourceNotFoundException("Meal not found"));

		final List<LocalDate> touched = new ArrayList<>();
		touched.add(mealToDelete.getPlannedDate());

		if (mealToDelete.getPlacement() == MealPlacement.COOK) {
			plannedMealRepository.findAllByBatchId(mealToDelete.getBatch().getId()).forEach(meal -> {
				touched.add(meal.getPlannedDate());
				plannedMealRepository.delete(meal);
			});
		} else {
			plannedMealRepository.delete(mealToDelete);
		}

		return mutation(resolveWeek(weekHint, mealToDelete.getPlannedDate()), touched);
	}

	@Transactional
	public PlanMutation deleteBatch(final UUID batchId, final LocalDate weekHint) {
		final CookBatchEntity batch = cookBatchRepository.findById(batchId)
						.orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

		final List<PlannedMealEntity> meals = plannedMealRepository.findAllByBatchId(batchId);
		final List<LocalDate> touched = new ArrayList<>(meals.stream()
						.map(PlannedMealEntity::getPlannedDate)
						.toList());

		// Deleted explicitly rather than left to the table's ON DELETE CASCADE, which exists for a
		// recipe being deleted from under the planner, so Hibernate's own state stays in step.
		plannedMealRepository.deleteAll(meals);
		cookBatchRepository.delete(batch);

		return mutation(resolveWeek(weekHint, cookDateOf(meals)), touched);
	}

	// ----------------------------------------------------------------- the week

	/**
	 * One assembler for every endpoint. A batch appears here when at least one of its meals sits in
	 * the week, but it is always reported with <em>all</em> of its meals: a batch cooked on Sunday
	 * with a leftover in the following week otherwise reports a different spare count on each screen.
	 */
	private PlanWeekDto assembleWeek(final LocalDate monday) {
		final LocalDate sunday = monday.plusDays(6);

		final Set<UUID> batchIds = plannedMealRepository.findAllByPlannedDateBetween(monday, sunday)
						.stream()
						.map(meal -> meal.getBatch().getId())
						.collect(Collectors.toCollection(LinkedHashSet::new));

		final Map<UUID, List<PlannedMealEntity>> mealsByBatch = batchIds.isEmpty()
						? Map.of()
						: plannedMealRepository.findAllByBatchIdIn(batchIds).stream()
						.collect(Collectors.groupingBy(meal -> meal.getBatch().getId()));

		final List<PlanBatchDto> batches = (batchIds.isEmpty()
						? List.<CookBatchEntity>of()
						: cookBatchRepository.findAllById(batchIds))
						.stream()
						.map(batch -> toBatchDto(batch, mealsByBatch.getOrDefault(batch.getId(), List.of())))
						.sorted(Comparator
										.comparing(PlanService::cookDateOfDto,
														Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(PlanBatchDto::getRecipeName,
														Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(PlanBatchDto::getId))
						.toList();

		// The queue and the freezer are global rather than week-scoped, so every week response carries
		// the same truth for them. A queued batch has no meals at all, so it needs no meal lookup.
		final List<PlanBatchDto> queue = cookBatchRepository.findQueued().stream()
						.map(batch -> toBatchDto(batch, List.of()))
						.sorted(Comparator.comparing(PlanBatchDto::getRecipeName,
														Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(PlanBatchDto::getId))
						.toList();

		final List<FrozenMealDto> freezer = toFreezer(
						plannedMealRepository.findAllByPlacement(MealPlacement.FROZEN))
						.stream()
						.sorted(Comparator
										.comparing(FrozenMealDto::getCookedOn,
														Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(FrozenMealDto::getId))
						.toList();

		return PlanWeekDto.builder()
						.weekStart(monday)
						.weekEnd(sunday)
						.cookingFor(householdSize())
						.batches(batches)
						.queue(queue)
						.freezer(freezer)
						.build();
	}

	private PlanBatchDto toBatchDto(final CookBatchEntity batch,
	                                final List<PlannedMealEntity> meals) {
		final RecipeEntity recipe = batch.getRecipe();
		final boolean queued = cookOf(meals).isEmpty();

		return PlanBatchDto.builder()
						.id(batch.getId())
						.recipeId(recipe.getId())
						.recipeName(recipe.getName())
						.recipeImageUrl(recipe.getImageUrl())
						.recipeYield(recipe.getRecipeYield())
						.servings(recipe.getServings())
						.cookingFor(batch.getCookingFor())
						.mealsTotal(batch.getMealsTotal())
						// A queued batch's meals cannot be placed anywhere - every day would be before the
						// cook night - so counting them into the pool would advertise meals nothing can spend.
						.spareMeals(queued ? 0 : Math.max(0, batch.getMealsTotal() - meals.size()))
						.meals(meals.stream()
										.sorted(MEAL_ORDER)
										.map(meal -> PlannedMealDto.builder()
														.id(meal.getId())
														.placement(meal.getPlacement())
														.plannedDate(meal.getPlannedDate())
														.build())
										.toList())
						.build();
	}

	/**
	 * The freezer panel. Each row carries its recipe and the night the pot was cooked, so the panel
	 * renders without cross-referencing a batch that usually belongs to some other week. The cook
	 * dates are looked up in one query rather than one per portion.
	 */
	private List<FrozenMealDto> toFreezer(final List<PlannedMealEntity> frozenMeals) {
		if (frozenMeals.isEmpty()) {
			return List.of();
		}

		final Set<UUID> batchIds = frozenMeals.stream()
						.map(frozen -> frozen.getBatch().getId())
						.collect(Collectors.toCollection(LinkedHashSet::new));

		final Map<UUID, LocalDate> cookDates = plannedMealRepository
						.findAllByBatchIdInAndPlacement(batchIds, MealPlacement.COOK).stream()
						.collect(Collectors.toMap(cook -> cook.getBatch().getId(),
										PlannedMealEntity::getPlannedDate));

		return frozenMeals.stream().map(frozen -> {
			final CookBatchEntity batch = frozen.getBatch();
			final RecipeEntity recipe = batch.getRecipe();

			return FrozenMealDto.builder()
							.id(frozen.getId())
							.batchId(batch.getId())
							.recipeId(recipe.getId())
							.recipeName(recipe.getName())
							.recipeImageUrl(recipe.getImageUrl())
							.cookedOn(cookDates.get(batch.getId()))
							.build();
		}).toList();
	}

	// ------------------------------------------------------------- shared rules

	/**
	 * How many meals one pot makes, at the household it is being cooked for. A snapshot: it is stored
	 * on the batch rather than divided again on every read.
	 */
	static int mealsTotalFor(final Integer servings, final int cookingFor) {
		if (servings == null) {
			// Nothing readable in the recipe's yield, so assume one dinner and let the client override.
			return MIN_MEALS_TOTAL;
		}
		return Math.max(MIN_MEALS_TOTAL, (int) Math.round((double) servings / cookingFor));
	}

	private void requirePlaceable(final MealPlacement placement,
	                              final LocalDate plannedDate,
	                              final LocalDate cookDate) {
		if (placement == null) {
			throw new BadRequestException("A meal needs a placement");
		}
		if (placement == MealPlacement.COOK) {
			throw new BadRequestException("A batch's cook night is set by its cook date");
		}
		if (cookDate == null) {
			throw new BadRequestException("You cannot eat or freeze from a batch with no cook night");
		}
		if (placement == MealPlacement.FROZEN) {
			if (plannedDate != null) {
				throw new BadRequestException("A frozen meal is not on a day");
			}
			return;
		}
		if (plannedDate == null) {
			throw new BadRequestException("A leftover needs a day");
		}
		if (!plannedDate.isAfter(cookDate)) {
			throw new BadRequestException("There is nothing to eat before it is cooked on " + cookDate);
		}
	}

	private int requireHousehold(final int cookingFor) {
		if (cookingFor < Household.MIN || cookingFor > Household.MAX) {
			throw new BadRequestException("You can only cook for between "
							+ Household.MIN + " and " + Household.MAX + " people");
		}
		return cookingFor;
	}

	private int householdSize() {
		return Household.parse(optionService.getOption(Option.PLAN_PORTION_SIZE).getValue());
	}

	private static Optional<PlannedMealEntity> cookOf(final List<PlannedMealEntity> meals) {
		return meals.stream()
						.filter(meal -> meal.getPlacement() == MealPlacement.COOK)
						.findFirst();
	}

	private static LocalDate cookDateOf(final List<PlannedMealEntity> meals) {
		return cookOf(meals).map(PlannedMealEntity::getPlannedDate).orElse(null);
	}

	private static LocalDate cookDateOfDto(final PlanBatchDto batch) {
		return batch.getMeals().stream()
						.filter(meal -> meal.getPlacement() == MealPlacement.COOK)
						.map(PlannedMealDto::getPlannedDate)
						.findFirst()
						.orElse(null);
	}

	/**
	 * The batch's leftovers that a cook night moved to {@code cookDate} now sits after.
	 */
	private static List<PlannedMealEntity> stranded(final List<PlannedMealEntity> meals,
	                                                final LocalDate cookDate) {
		return meals.stream()
						.filter(meal -> meal.getPlacement() == MealPlacement.LEFTOVER)
						.filter(meal -> !meal.getPlannedDate().isAfter(cookDate))
						.toList();
	}

	private static PlannedMealEntity meal(final CookBatchEntity batch,
	                                      final MealPlacement placement,
	                                      final LocalDate plannedDate) {
		return PlannedMealEntity.builder()
						.batch(batch)
						.placement(placement)
						.plannedDate(plannedDate)
						.build();
	}

	// ------------------------------------------------------------------- weeks

	/**
	 * Which week to answer with. {@code ?week=} is what the client has on screen; without it, the week
	 * of whatever date the mutation touched, and failing that the current one.
	 */
	private LocalDate resolveWeek(final LocalDate weekHint, final LocalDate fallback) {
		if (weekHint != null) {
			return mondayOf(weekHint);
		}
		return mondayOf(fallback != null ? fallback : LocalDate.now(clock));
	}

	/**
	 * Assembles the response week and names every week the mutation touched, so the broadcast can
	 * invalidate a week the client is not looking at - a leftover moved into next week, say.
	 */
	private PlanMutation mutation(final LocalDate responseWeek, final List<LocalDate> touchedDates) {
		final Set<LocalDate> weekStarts = new LinkedHashSet<>();
		weekStarts.add(responseWeek);
		touchedDates.stream()
						.filter(Objects::nonNull)
						.map(PlanService::mondayOf)
						.forEach(weekStarts::add);

		return new PlanMutation(assembleWeek(responseWeek), List.copyOf(weekStarts));
	}
}
