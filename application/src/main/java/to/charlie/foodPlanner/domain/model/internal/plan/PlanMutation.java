package to.charlie.foodPlanner.domain.model.internal.plan;

import java.time.LocalDate;
import java.util.List;
import to.charlie.foodPlanner.domain.model.dto.plan.PlanWeekDto;

/**
 * What a plan mutation produced: the week to answer the request with, and every week it touched.
 *
 * <p>The two are not the same. Moving a leftover into next week, or stranding one that then falls
 * before its cook night, changes a week the client is not looking at, and the broadcast has to name
 * it so other devices drop their copy.
 *
 * <p>The weeks travel back to the controller rather than being broadcast from the service because
 * {@code @Transactional} commits when the annotated method returns to its caller: a broadcast on the
 * service's last line still runs pre-commit, and a phone that reacted by immediately re-reading the
 * week would read the old state.
 */
public record PlanMutation(PlanWeekDto week, List<LocalDate> affectedWeekStarts) {
}
