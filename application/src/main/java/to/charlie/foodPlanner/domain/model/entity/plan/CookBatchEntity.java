package to.charlie.foodPlanner.domain.model.entity.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

/**
 * One pot of one recipe, producing {@code mealsTotal} meals.
 *
 * <p>{@code mealsTotal} and {@code cookingFor} are a snapshot taken when the batch is planned rather
 * than a live division of the recipe's yield: editing a recipe's yield, or changing the household
 * size, must not silently invalidate a leftover that has already been eaten or a portion that is
 * physically in the freezer.
 *
 * <p>The recipe FK cascades in the database rather than through JPA, so deleting a recipe through
 * the existing endpoint takes its batches and their meals with it.
 */
@Table(name = "cook_batch")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CookBatchEntity {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(name = "meals_total", nullable = false)
  private int mealsTotal;

  @Column(name = "cooking_for", nullable = false)
  private int cookingFor;

  @Builder.Default
  @CreationTimestamp
  @Column(name = "created_at_time")
  private LocalDateTime createdAtTime = LocalDateTime.now();

  @UpdateTimestamp
  @Column(name = "updated_at_time")
  private LocalDateTime updatedAtTime;
}
