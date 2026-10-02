package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.plan.PlannedMealEntity;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

@Repository
public interface PlannedMealRepository extends JpaRepository<PlannedMealEntity, UUID> {

  /** The week read starts here: every meal sitting on a day in range. Frozen meals have no date. */
  List<PlannedMealEntity> findAllByPlannedDateBetween(LocalDate from, LocalDate to);

  /**
   * All of a batch's meals, including the ones outside the week being read. A batch cooked on Sunday
   * with a leftover in the following week otherwise reports the wrong spare count on both screens.
   */
  List<PlannedMealEntity> findAllByBatchIdIn(Collection<UUID> batchIds);

  List<PlannedMealEntity> findAllByPlacement(MealPlacement placement);

  List<PlannedMealEntity> findAllByBatchId(UUID batchId);

  List<PlannedMealEntity> findAllByBatchIdInAndPlacement(
      Collection<UUID> batchIds, MealPlacement placement);

  /** The cook nights from this week onwards, which is the scope a household change reshapes. */
  List<PlannedMealEntity> findAllByPlacementAndPlannedDateGreaterThanEqual(
      MealPlacement placement, LocalDate from);
}
