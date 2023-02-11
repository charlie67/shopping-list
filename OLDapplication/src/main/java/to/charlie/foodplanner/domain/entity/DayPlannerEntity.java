package to.charlie.foodplanner.domain.entity;

import java.util.Date;
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
@Table(name = "planner")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayPlannerEntity
{
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private UUID id;

  private Date day;

  @ManyToMany
  @JoinTable(name = "planner_recipes",
      joinColumns = @JoinColumn(name = "day_planner_entity_id"),
      inverseJoinColumns = @JoinColumn(name = "recipes_id"))
  @ToString.Exclude
  private Set<RecipeEntity> recipes = new LinkedHashSet<>();
}
