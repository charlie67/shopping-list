package to.charlie.foodPlanner.domain.model.converter.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import to.charlie.foodPlanner.domain.model.dto.IngredientDto;
import to.charlie.foodPlanner.domain.model.dto.recipe.RecipeDto;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;

@ExtendWith(MockitoExtension.class)
class RecipeEntityToDtoConverterTest {

    @Mock
    private Converter<RecipeIngredientEntity, IngredientDto> ingredientConverter;

    private RecipeEntityToDtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RecipeEntityToDtoConverter(ingredientConverter);
    }

    @Test
    void convert_whenEntityHasNoIngredients_thenMapsBasicFields() {
        // given
        UUID id = UUID.randomUUID();
        RecipeEntity entity = RecipeEntity.builder()
                .id(id)
                .name("Pasta Carbonara")
                .url("https://example.com/pasta")
                .ingredients(new LinkedHashSet<>())
                .build();

        // when
        RecipeDto dto = converter.convert(entity);

        // then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Pasta Carbonara");
        assertThat(dto.getUrl()).isEqualTo("https://example.com/pasta");
        assertThat(dto.getIngredients()).isEmpty();
    }

    @Test
    void convert_whenEntityHasIngredients_thenDelegatesEachIngredientToConverter() {
        // given
        RecipeIngredientEntity ingredientEntity = RecipeIngredientEntity.builder()
                .id(UUID.randomUUID())
                .quantity(200.0)
                .unit(MeasurementUnit.GRAMS)
                .ingredient(IngredientEntity.builder().name("spaghetti").build())
                .build();

        IngredientDto ingredientDto = IngredientDto.builder()
                .ingredientName("spaghetti")
                .quantity(200.0)
                .unit(MeasurementUnit.GRAMS)
                .build();

        when(ingredientConverter.convert(ingredientEntity)).thenReturn(ingredientDto);

        RecipeEntity entity = RecipeEntity.builder()
                .id(UUID.randomUUID())
                .name("Spaghetti")
                .url("https://example.com")
                .ingredients(new LinkedHashSet<>(Set.of(ingredientEntity)))
                .build();

        // when
        RecipeDto dto = converter.convert(entity);

        // then
        assertThat(dto.getIngredients()).hasSize(1);
        assertThat(dto.getIngredients()).contains(ingredientDto);
    }
}
