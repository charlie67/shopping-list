package to.charlie.foodPlanner.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.extraction.recipe.RecipeExtractor;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;

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

    @Test
    void extractRecipeUsing_whenMethodRequested_thenOnlyThatExtractorIsUsed()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        ExtractedRecipe expectedRecipe = ExtractedRecipe.builder().name("Pasta").build();
        when(firstExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JSON_LD);
        when(secondExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JUST_THE_RECIPE);
        when(secondExtractor.extract(document, url)).thenReturn(expectedRecipe);

        // when
        ExtractedRecipe result =
                service.extractRecipeUsing(document, url, ExtractionMethod.JUST_THE_RECIPE);

        // then
        assertThat(result).isEqualTo(expectedRecipe);
        verify(firstExtractor, never()).extract(any(), any());
    }

    @Test
    void extractRecipeUsing_whenRequestedExtractorFails_thenThrowsWithoutTryingAnother()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        when(firstExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JSON_LD);
        when(secondExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JUST_THE_RECIPE);
        when(secondExtractor.extract(document, url)).thenThrow(new RecipeExtractionFailed("failed"));

        // when / then
        assertThatThrownBy(
                () -> service.extractRecipeUsing(document, url, ExtractionMethod.JUST_THE_RECIPE))
                .isInstanceOf(RecipeExtractionFailed.class)
                .hasMessage("Unable to extract recipe from " + url + " using JustTheRecipe");
        verify(firstExtractor, never()).extract(any(), any());
    }

    @Test
    void extractRecipeUsing_whenRequestedExtractorThrowsRuntimeException_thenThrowsExtractionFailed()
            throws RecipeExtractionFailed {
        // given
        String url = "https://example.com/recipe";
        when(firstExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JUST_THE_RECIPE);
        when(firstExtractor.extract(document, url)).thenThrow(new RuntimeException("service down"));

        // when / then
        assertThatThrownBy(
                () -> service.extractRecipeUsing(document, url, ExtractionMethod.JUST_THE_RECIPE))
                .isInstanceOf(RecipeExtractionFailed.class);
        verify(secondExtractor, never()).extract(any(), any());
    }

    @Test
    void extractRecipeUsing_whenNoExtractorForMethod_thenThrowsIllegalArgumentException() {
        // given
        when(firstExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.JSON_LD);
        when(secondExtractor.getExtractionMethod()).thenReturn(ExtractionMethod.MICRODATA);

        // when / then
        assertThatThrownBy(() -> service.extractRecipeUsing(
                document, "https://example.com", ExtractionMethod.JUST_THE_RECIPE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No recipe extractor for method JUST_THE_RECIPE");
    }
}
