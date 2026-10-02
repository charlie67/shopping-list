package to.charlie.foodPlanner.domain.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;
import to.charlie.foodPlanner.domain.model.internal.options.Option;

@Table(name = "options")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OptionEntity {

  @Id
  @Enumerated(EnumType.STRING)
  private Option name;

  private String value;

  @UpdateTimestamp
  private LocalDateTime updatedAtTime;
}
