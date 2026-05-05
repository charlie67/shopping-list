package to.charlie.foodPlanner.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.extraction.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@ExtendWith(MockitoExtension.class)
class RecipeExtractionServiceTest {

    @Mock
    private RecipeExtractor firstExtractor;

    @Mock
    private RecipeExtractor secondExtractor;

    @Mock
    private Document document;

    private RecipeExtractionService service;

    @BeforeEach
    void setUp() {
        service = new RecipeExtractionService(List.of(firstExtractor, secondExtractor));
    }

    @Test
    void extractRecipe_whenFirstExtractorSucceeds_thenReturnsRecipeWithoutTryingSecond()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        ExtractedRecipe expectedRecipe = ExtractedRecipe.builder().name("Pasta").build();
        when(firstExtractor.extract(document, url)).thenReturn(expectedRecipe);

        // when
        ExtractedRecipe result = service.extractRecipe(document, url);

        // then
        assertThat(result).isEqualTo(expectedRecipe);
        verify(firstExtractor).extract(document, url);
    }

    @Test
    void extractRecipe_whenFirstExtractorThrowsRecipeExtractionFailed_thenTriesSecondExtractor()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        ExtractedRecipe expectedRecipe = ExtractedRecipe.builder().name("Pasta").build();
        when(firstExtractor.extract(document, url)).thenThrow(new RecipeExtractionFailed("not found"));
        when(secondExtractor.extract(document, url)).thenReturn(expectedRecipe);

        // when
        ExtractedRecipe result = service.extractRecipe(document, url);

        // then
        assertThat(result).isEqualTo(expectedRecipe);
        verify(secondExtractor).extract(document, url);
    }

    @Test
    void extractRecipe_whenAllExtractorsFail_thenThrowsIllegalArgumentException()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        when(firstExtractor.extract(document, url)).thenThrow(new RecipeExtractionFailed("failed"));
        when(secondExtractor.extract(document, url)).thenThrow(new RecipeExtractionFailed("failed"));

        // when / then
        assertThatThrownBy(() -> service.extractRecipe(document, url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exhausted all recipe extractors");
    }

    @Test
    void extractRecipe_whenExtractorThrowsUnhandledException_thenContinuesToNextExtractor()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        ExtractedRecipe expectedRecipe = ExtractedRecipe.builder().name("Pasta").build();
        when(firstExtractor.extract(document, url)).thenThrow(new RuntimeException("unexpected"));
        when(secondExtractor.extract(document, url)).thenReturn(expectedRecipe);

        // when
        ExtractedRecipe result = service.extractRecipe(document, url);

        // then
        assertThat(result).isEqualTo(expectedRecipe);
    }

    @Test
    void extractRecipe_whenNoExtractors_thenThrowsIllegalArgumentException() {
        // given
        service = new RecipeExtractionService(List.of());

        // when / then
        assertThatThrownBy(() -> service.extractRecipe(document, "https://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
