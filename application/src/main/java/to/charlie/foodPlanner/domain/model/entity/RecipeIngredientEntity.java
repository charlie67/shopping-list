package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

import java.util.UUID;

@Table(name = "recipe_ingredient")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RecipeIngredientEntity {

	@Id
	@GeneratedValue
	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "quantity")
	private double quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "unit")
	private MeasurementUnit unit;

	@OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
	@JoinColumn(name = "ingredient_id", nullable = false)
	private IngredientEntity ingredient;

	@ManyToOne
	@JoinColumn(name = "recipe_id", nullable = false)
	private RecipeEntity recipe;
}
