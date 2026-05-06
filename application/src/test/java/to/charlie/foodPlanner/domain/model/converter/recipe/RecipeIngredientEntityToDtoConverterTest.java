package to.charlie.foodPlanner.domain.model.converter.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

class RecipeIngredientEntityToDtoConverterTest {

    private RecipeIngredientEntityToDtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RecipeIngredientEntityToDtoConverter();
    }

    @Test
    void convert_whenValidEntity_thenMapsAllFieldsIncludingIngredientName() {
        // given
        UUID id = UUID.randomUUID();
        IngredientEntity ingredient = IngredientEntity.builder()
                .id(UUID.randomUUID())
                .name("flour")
                .build();

        RecipeIngredientEntity entity = RecipeIngredientEntity.builder()
                .id(id)
                .quantity(2.0)
                .unit(MeasurementUnit.CUPS)
                .ingredient(ingredient)
                .build();

        // when
        IngredientDto dto = converter.convert(entity);

        // then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getIngredientName()).isEqualTo("flour");
        assertThat(dto.getQuantity()).isEqualTo(2.0);
        assertThat(dto.getUnit()).isEqualTo(MeasurementUnit.CUPS);
    }

    @Test
    void convert_whenEntityHasUnknownUnit_thenMapsUnknownUnit() {
        // given
        IngredientEntity ingredient = IngredientEntity.builder()
                .id(UUID.randomUUID())
                .name("salt")
                .build();

        RecipeIngredientEntity entity = RecipeIngredientEntity.builder()
                .id(UUID.randomUUID())
                .quantity(0.0)
                .unit(MeasurementUnit.UNKNOWN)
                .ingredient(ingredient)
                .build();

        // when
        IngredientDto dto = converter.convert(entity);

        // then
        assertThat(dto.getIngredientName()).isEqualTo("salt");
        assertThat(dto.getUnit()).isEqualTo(MeasurementUnit.UNKNOWN);
    }
}
