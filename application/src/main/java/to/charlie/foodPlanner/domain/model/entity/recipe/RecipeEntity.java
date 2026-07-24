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
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "description")
  private String description;

  @Column(name = "name")
  private String name;

  @Column(name = "url")
  private String url;

  @Column(name = "date_modified")
  private String dateModified;

  @Column(name = "date_published")
  private String datePublished;

  @Column(name = "keywords")
  private String keywords;

  @Column(name = "cook_time")
  private String cookTime;

  @Column(name = "prep_time")
  private String prepTime;

  @Column(name = "total_time")
  private String totalTime;

  @Column(name = "recipe_category")
  private String recipeCategory;

  @Column(name = "recipe_yield")
  private String recipeYield;

  @Column(name = "calories")
  private String calories;

  @Column(name = "fat_content")
  private String fatContent;

  @Column(name = "saturated_fat_content")
  private String saturatedFatContent;

  @Column(name = "carbohydrate_content")
  private String carbohydrateContent;

  @Column(name = "sugar_content")
  private String sugarContent;

  @Column(name = "fiber_content")
  private String fiberContent;

  @Column(name = "protein_content")
  private String proteinContent;

  @Column(name = "sodium_content")
  private String sodiumContent;

  @Enumerated(EnumType.STRING)
  @Column(name = "extraction_method")
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
  @Column(name = "created_at_time")
  private LocalDateTime createdAtTime = LocalDateTime.now();

  @UpdateTimestamp
  @Column(name = "updated_at_time")
  private LocalDateTime updatedAtTime;
}
