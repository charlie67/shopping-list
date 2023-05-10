package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Table(name = "ingredient")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class IngredientEntity {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;
}
