package to.charlie.foodPlanner.infrastructure.dal.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import to.charlie.foodPlanner.domain.model.converter.recipe.ExtractedRecipeToRecipeEntityConverter;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.infrastructure.dal.repository.PageableRecipeRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeDaoTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private PageableRecipeRepository pageableRecipeRepository;

    @Mock
    private ExtractedRecipeToRecipeEntityConverter extractedRecipeConverter;

    @Mock
    private ModelMapper modelMapper;

    private RecipeDao recipeDao;

    @BeforeEach
    void setUp() {
        recipeDao = new RecipeDao(recipeRepository, pageableRecipeRepository, extractedRecipeConverter, modelMapper);
    }

    private RecipeEntity buildRecipeEntity(final UUID id, final String name, final String url) {
        return RecipeEntity.builder().id(id).name(name).url(url).build();
    }

    @Test
    void save_whenRecipeEntityProvided_thenDelegatesToRepositoryAndReturnsResult() {
        // given
        final RecipeEntity entity = buildRecipeEntity(UUID.randomUUID(), "Pasta", "https://example.com");
        when(recipeRepository.save(entity)).thenReturn(entity);

        // when
        final RecipeEntity result = recipeDao.save(entity);

        // then
        assertThat(result).isEqualTo(entity);
        verify(recipeRepository).save(entity);
    }

    @Test
    void save_whenExtractedRecipeProvided_thenConvertsAndSaves() {
        // given
        final ExtractedRecipe extractedRecipe = ExtractedRecipe.builder()
                .name("Soup")
                .url("https://example.com/soup")
                .extractedRecipeIngredients(List.of())
                .extractedRecipeInstructions(List.of())
                .build();
        final RecipeEntity convertedEntity = buildRecipeEntity(UUID.randomUUID(), "Soup", "https://example.com/soup");
        when(extractedRecipeConverter.convert(extractedRecipe)).thenReturn(convertedEntity);
        when(recipeRepository.save(convertedEntity)).thenReturn(convertedEntity);

        // when
        final RecipeEntity result = recipeDao.save(extractedRecipe);

        // then
        assertThat(result).isEqualTo(convertedEntity);
        verify(extractedRecipeConverter).convert(extractedRecipe);
        verify(recipeRepository).save(convertedEntity);
    }

    @Test
    void findAll_whenRecipesExist_thenReturnsMappedDtos() {
        // given
        final RecipeEntity entity = buildRecipeEntity(UUID.randomUUID(), "Curry", "https://example.com/curry");
        final ExtractedRecipeDto dto = ExtractedRecipeDto.builder().name("Curry").build();
        when(recipeRepository.findAll()).thenReturn(List.of(entity));
        when(modelMapper.map(entity, ExtractedRecipeDto.class)).thenReturn(dto);

        // when
        final List<ExtractedRecipeDto> result = recipeDao.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Curry");
    }

    @Test
    void findAll_whenNoRecipesExist_thenReturnsEmptyList() {
        // given
        when(recipeRepository.findAll()).thenReturn(List.of());

        // when
        final List<ExtractedRecipeDto> result = recipeDao.findAll();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByUrl_whenUrlExists_thenReturnsTrue() {
        // given
        final String url = "https://example.com/recipe";
        when(recipeRepository.existsByUrl(url)).thenReturn(true);

        // when
        final boolean result = recipeDao.existsByUrl(url);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByUrl_whenUrlDoesNotExist_thenReturnsFalse() {
        // given
        final String url = "https://example.com/missing";
        when(recipeRepository.existsByUrl(url)).thenReturn(false);

        // when
        final boolean result = recipeDao.existsByUrl(url);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findPage_whenPageRequested_thenReturnsMappedPageOfDtos() {
        // given
        final RecipeEntity entity = buildRecipeEntity(UUID.randomUUID(), "Risotto", "https://example.com/risotto");
        final ExtractedRecipeDto dto = ExtractedRecipeDto.builder().name("Risotto").build();
        final Page<RecipeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(pageableRecipeRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(modelMapper.map(eq(entity), eq(ExtractedRecipeDto.class))).thenReturn(dto);

        // when
        final Page<ExtractedRecipeDto> result = recipeDao.findPage(0);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Risotto");
    }

    @Test
    void findByUrl_whenUrlExists_thenReturnsMappedExtractedRecipe() {
        // given
        final String url = "https://example.com/found";
        final RecipeEntity entity = buildRecipeEntity(UUID.randomUUID(), "Stew", url);
        final ExtractedRecipe extractedRecipe = ExtractedRecipe.builder().name("Stew").url(url).build();
        when(recipeRepository.findByUrl(url)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ExtractedRecipe.class)).thenReturn(extractedRecipe);

        // when
        final Optional<ExtractedRecipe> result = recipeDao.findByUrl(url);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Stew");
    }

    @Test
    void findByUrl_whenUrlDoesNotExist_thenReturnsEmpty() {
        // given
        final String url = "https://example.com/notfound";
        when(recipeRepository.findByUrl(url)).thenReturn(Optional.empty());

        // when
        final Optional<ExtractedRecipe> result = recipeDao.findByUrl(url);

        // then
        assertThat(result).isEmpty();
    }
}
