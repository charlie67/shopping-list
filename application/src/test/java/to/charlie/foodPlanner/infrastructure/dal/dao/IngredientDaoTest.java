package to.charlie.foodPlanner.infrastructure.dal.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.model.entity.IngredientEntity;
import to.charlie.foodPlanner.infrastructure.dal.repository.IngredientRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientDaoTest {

    @Mock
    private IngredientRepository ingredientRepository;

    private IngredientDao ingredientDao;

    @BeforeEach
    void setUp() {
        ingredientDao = new IngredientDao(ingredientRepository);
    }

    @Test
    void findOrCreateIngredient_whenIngredientExists_thenReturnsExistingWithoutSaving() {
        // given
        final String name = "Tomato";
        final IngredientEntity existing = IngredientEntity.builder().id(UUID.randomUUID()).name(name).build();
        when(ingredientRepository.findByName(name)).thenReturn(Optional.of(existing));

        // when
        final IngredientEntity result = ingredientDao.findOrCreateIngredient(name);

        // then
        assertThat(result).isEqualTo(existing);
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void findOrCreateIngredient_whenIngredientDoesNotExist_thenCreatesAndSavesNewIngredient() {
        // given
        final String name = "Garlic";
        when(ingredientRepository.findByName(name)).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(IngredientEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        final IngredientEntity result = ingredientDao.findOrCreateIngredient(name);

        // then
        assertThat(result.getName()).isEqualTo(name);
        final ArgumentCaptor<IngredientEntity> captor = ArgumentCaptor.forClass(IngredientEntity.class);
        verify(ingredientRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(name);
    }
}
