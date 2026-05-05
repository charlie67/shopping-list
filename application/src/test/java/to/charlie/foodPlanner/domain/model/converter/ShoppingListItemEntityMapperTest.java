package to.charlie.foodPlanner.domain.model.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;

class ShoppingListItemEntityMapperTest {

    @Test
    void entityToDto_whenValidEntity_thenMapsAllFields() {
        // given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 16, 12, 0, 0);

        ShoppingListItemEntity entity = ShoppingListItemEntity.builder()
                .id(id)
                .title("Milk")
                .completed(false)
                .createdAtTime(createdAt)
                .updatedAtTime(updatedAt)
                .build();

        // when
        ShoppingListItemDto dto = ShoppingListItemEntityMapper.entityToDto(entity);

        // then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Milk");
        assertThat(dto.isCompleted()).isFalse();
        assertThat(dto.getCreatedAtTime()).isEqualTo(createdAt.toEpochSecond(ZoneOffset.UTC));
        assertThat(dto.getUpdatedAtTime()).isEqualTo(updatedAt.toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void entityToDto_whenEntityIsCompleted_thenMapsCompletedTrue() {
        // given
        ShoppingListItemEntity entity = ShoppingListItemEntity.builder()
                .id(UUID.randomUUID())
                .title("Eggs")
                .completed(true)
                .createdAtTime(LocalDateTime.now())
                .updatedAtTime(LocalDateTime.now())
                .build();

        // when
        ShoppingListItemDto dto = ShoppingListItemEntityMapper.entityToDto(entity);

        // then
        assertThat(dto.isCompleted()).isTrue();
    }

    @Test
    void entityToDto_whenTimestampsAreEpoch_thenConvertsToCorrectEpochSeconds() {
        // given
        LocalDateTime epoch = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

        ShoppingListItemEntity entity = ShoppingListItemEntity.builder()
                .id(UUID.randomUUID())
                .title("Bread")
                .createdAtTime(epoch)
                .updatedAtTime(epoch)
                .build();

        // when
        ShoppingListItemDto dto = ShoppingListItemEntityMapper.entityToDto(entity);

        // then
        assertThat(dto.getCreatedAtTime()).isEqualTo(0L);
        assertThat(dto.getUpdatedAtTime()).isEqualTo(0L);
    }
}
