package to.charlie.foodPlanner.infrastructure.dal.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeIngredientEntity;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeIngredientRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeIngredientDaoTest {

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    private RecipeIngredientDao recipeIngredientDao;

    @BeforeEach
    void setUp() {
        recipeIngredientDao = new RecipeIngredientDao(recipeIngredientRepository);
    }

    @Test
    void save_whenRecipeIngredientEntityProvided_thenDelegatesToRepositoryAndReturnsResult() {
        // given
        final RecipeIngredientEntity entity = RecipeIngredientEntity.builder()
                .wholeText("200g flour")
                .build();
        when(recipeIngredientRepository.save(entity)).thenReturn(entity);

        // when
        final RecipeIngredientEntity result = recipeIngredientDao.save(entity);

        // then
        assertThat(result).isEqualTo(entity);
        verify(recipeIngredientRepository).save(entity);
    }
}
