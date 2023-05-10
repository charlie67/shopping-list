package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
	private long id;

	@NotEmpty(message = "Title is required")
	private String title;

	@Builder.Default
	private boolean completed = false;

	@Builder.Default
	private int quantity = 1;

	@Builder.Default
	@CreationTimestamp
	private LocalDateTime createdAtTime = LocalDateTime.now();

	public ShoppingListItemEntity(final String title) {
		this.title = title;
	}
}
