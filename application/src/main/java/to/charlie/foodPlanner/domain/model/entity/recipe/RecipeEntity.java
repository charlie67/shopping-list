package to.charlie.foodPlanner.domain.model.entity.recipe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
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
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

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
  @Column(nullable = false)
  private UUID id;

  @Column
  private String description;

  @Column
  private String name;

  @Column
  private String url;

  @Column
  private String dateModified;

  @Column
  private String datePublished;

  @Column
  private String keywords;

  @Column
  private String cookTime;

  @Column
  private String prepTime;

  @Column
  private String totalTime;

  @Column
  private String recipeCategory;

  @Column
  private String recipeYield;

  /**
   * recipeYield read as a number, for the planner's portion maths. Null when the scraped text held no
   * usable figure. Always derived by RecipeYieldParser on save, never taken from the client.
   */
  @Column
  private Integer servings;

  @Column
  private String calories;

  @Column
  private String fatContent;

  @Column
  private String saturatedFatContent;

  @Column
  private String carbohydrateContent;

  @Column
  private String sugarContent;

  @Column
  private String fiberContent;

  @Column
  private String proteinContent;

  @Column
  private String sodiumContent;

  @Enumerated(EnumType.STRING)
  @Column
  private ExtractionMethod extractionMethod;

  private String imageUrl;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("ingredientOrder")
  private Set<RecipeIngredientEntity> ingredients = new LinkedHashSet<>();

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("stepCount")
  private Set<RecipeStepEntity> steps = new LinkedHashSet<>();

  @Builder.Default
  @CreationTimestamp
  @Column
  private LocalDateTime createdAtTime = LocalDateTime.now();

  @UpdateTimestamp
  @Column
  private LocalDateTime updatedAtTime;
}
