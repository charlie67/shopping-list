package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ShoppingListItemEntity {

  @Id @GeneratedValue private long id;

  @NotEmpty(message = "Title is required")
  private String title;

  @Builder.Default private boolean completed = false;

  @Builder.Default private int quantity = 1;

  @Builder.Default @CreationTimestamp private LocalDateTime createdAtTime = LocalDateTime.now();

  public ShoppingListItemEntity(String title) {
    this.title = title;
  }
}
