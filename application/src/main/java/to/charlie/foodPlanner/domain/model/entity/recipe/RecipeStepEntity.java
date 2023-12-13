package to.charlie.foodPlanner.domain.model.entity.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Table(name = "recipe_steps")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RecipeStepEntity {
  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "type")
  private String type;

  @Column(name = "step_count", nullable = false)
  private int stepCount;

  @ManyToOne
  @JoinColumn(name = "recipe_id")
  private RecipeEntity recipe;
}
