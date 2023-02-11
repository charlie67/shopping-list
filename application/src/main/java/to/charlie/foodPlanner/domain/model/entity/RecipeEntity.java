package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "recipe")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RecipeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "title")
  private String title;

  @Column(name = "url")
  private String url;

  @Column(name = "instructions")
  private String instructions;

  @ManyToMany
  @JoinTable(
      name = "recipe_tags",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "tags_id"))
  private Set<TagEntity> tags = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "recipe_ingredients",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "ingredients_id"))
  @ToString.Exclude
  private Set<IngredientEntity> ingredients = new LinkedHashSet<>();
}
