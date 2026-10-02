package to.charlie.foodPlanner.infrastructure.dal.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.plan.CookBatchEntity;

@Repository
public interface CookBatchRepository extends JpaRepository<CookBatchEntity, UUID> {

  /**
   * The queue: batches nothing has been cooked from yet. Global rather than week-scoped, since a
   * batch with no cook night belongs to no week.
   *
   * <p>Written out because there is no derived name for "has no child row of this kind". The
   * alternative is loading every batch and filtering in Java, which is fine at this size but gets
   * worse silently.
   */
  @Query("select b from CookBatchEntity b where not exists "
      + "(select m from PlannedMealEntity m where m.batch = b and m.placement = 'COOK')")
  List<CookBatchEntity> findQueued();
}
