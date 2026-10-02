package to.charlie.foodPlanner.domain.model.entity.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import to.charlie.foodPlanner.domain.model.internal.plan.MealPlacement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "planned_meal")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PlannedMealEntity {

	@Id
	@GeneratedValue
	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "batch_id", nullable = false)
	private CookBatchEntity batch;

	@Enumerated(EnumType.STRING)
	@Column(name = "placement", nullable = false)
	private MealPlacement placement;

	@Column(name = "planned_date")
	private LocalDate plannedDate;

	@Builder.Default
	@CreationTimestamp
	@Column(name = "created_at_time")
	private LocalDateTime createdAtTime = LocalDateTime.now();

	@UpdateTimestamp
	@Column(name = "updated_at_time")
	private LocalDateTime updatedAtTime;
}
