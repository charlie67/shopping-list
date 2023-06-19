package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "shopping_list_item")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ShoppingListItemEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @NotEmpty(message = "Title is required")
  private String title;

  @Builder.Default
  private boolean completed = false;

  @Builder.Default
  private int quantity = 1;

  @Builder.Default
  @CreationTimestamp
  private LocalDateTime createdAtTime = LocalDateTime.now();

  @UpdateTimestamp
  private LocalDateTime updatedAtTime;
}
