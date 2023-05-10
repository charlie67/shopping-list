package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "recipe")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RecipeEntity {
	@Id
	@GeneratedValue
	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "title")
	private String title;

	@Column(name = "url")
	private String url;

	@Column(name = "instructions")
	private String instructions;

	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RecipeIngredientEntity> ingredients = new LinkedHashSet<>();

}
