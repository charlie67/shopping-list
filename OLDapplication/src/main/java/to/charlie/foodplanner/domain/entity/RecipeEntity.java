package to.charlie.foodplanner.domain.entity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "recipe")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeEntity
{
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
  @JoinTable(name = "recipe_tags",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "tags_id"))
  @ToString.Exclude
  private Set<TagEntity> tags = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(name = "recipe_ingredients",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "ingredients_id"))
  @ToString.Exclude
  private Set<IngredientEntity> ingredients = new LinkedHashSet<>();
}