package to.charlie.foodPlanner.infrastructure.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.service.RecipeService;
import to.charlie.foodPlanner.infrastructure.rest.controllers.RecipeController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

	@Mock
	private RecipeService recipeService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new RecipeController(recipeService)).build();
	}

	@Test
	void extract_whenUrlIsValidAndSaveIsTrue_thenReturnsOkWithRecipe() throws Exception {
		// given
		final String url = "https://example.com/recipe";
		final ExtractedRecipeDto dto = ExtractedRecipeDto.builder()
						.id(UUID.randomUUID())
						.name("Pasta")
						.url(url)
						.build();
		when(recipeService.extractRecipeFromUrl(url, true)).thenReturn(dto);

		// when / then
		mockMvc.perform(post("/recipe/extract")
										.param("url", url)
										.param("save", "true")
										.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isCreated())
						.andExpect(jsonPath("$.name").value("Pasta"));
	}

	@Test
	void extract_whenUrlIsValidAndSaveIsFalse_thenReturnsOkWithRecipe() throws Exception {
		// given
		final String url = "https://example.com/recipe";
		final ExtractedRecipeDto dto = ExtractedRecipeDto.builder()
						.id(UUID.randomUUID())
						.name("Soup")
						.url(url)
						.build();
		when(recipeService.extractRecipeFromUrl(url, false)).thenReturn(dto);

		// when / then
		mockMvc.perform(post("/recipe/extract")
										.param("url", url)
										.param("save", "false"))
						.andExpect(status().isCreated())
						.andExpect(jsonPath("$.name").value("Soup"));
	}

	@Test
	void extract_whenIoExceptionThrown_thenReturnsNotFound() throws Exception {
		// given
		final String url = "https://example.com/unreachable";
		when(recipeService.extractRecipeFromUrl(url, false)).thenThrow(new IOException("connection failed"));

		// when / then
		mockMvc.perform(post("/recipe/extract")
										.param("url", url)
										.param("save", "false"))
						.andExpect(status().isNotFound());
	}

	@Test
	void extract_whenIllegalArgumentExceptionThrown_thenReturnsInternalServerError() throws Exception {
		// given
		final String url = "https://example.com/bad";
		when(recipeService.extractRecipeFromUrl(url, false)).thenThrow(new IllegalArgumentException("bad url"));

		// when / then
		mockMvc.perform(post("/recipe/extract")
										.param("url", url)
										.param("save", "false"))
						.andExpect(status().isInternalServerError());
	}

	@Test
	void extract_whenDuplicateRecipeExceptionThrown_thenReturnsOkWithEmptyBody() throws Exception {
		// given
		final String url = "https://example.com/duplicate";
		when(recipeService.extractRecipeFromUrl(url, true)).thenThrow(new DuplicateRecipeException("already exists"));

		// when / then
		mockMvc.perform(post("/recipe/extract")
										.param("url", url)
										.param("save", "true"))
						.andExpect(status().isOk());
	}

	@Test
	void getRecipePage_whenPageIsValid_thenReturnsOkWithPage() throws Exception {
		// given
		final ExtractedRecipeDto dto = ExtractedRecipeDto.builder()
						.id(UUID.randomUUID())
						.name("Risotto")
						.build();
		final Page<ExtractedRecipeDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
		when(recipeService.getRecipePage(0)).thenReturn(page);

		// when / then
		mockMvc.perform(get("/recipe").param("page", "0"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.content[0].name").value("Risotto"));
	}

	@Test
	void getRecipePage_whenPageIsEmpty_thenReturnsOkWithEmptyPage() throws Exception {
		// given
		final Page<ExtractedRecipeDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(5, 20), 0);
		when(recipeService.getRecipePage(5)).thenReturn(emptyPage);

		// when / then
		mockMvc.perform(get("/recipe").param("page", "5"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.content").isEmpty());
	}

	@Test
	void getRecipe_whenRecipeExists_thenReturnsOkWithRecipe() throws Exception {
		// given
		final UUID id = UUID.randomUUID();
		final ExtractedRecipeDto dto = ExtractedRecipeDto.builder()
						.id(id)
						.name("Curry")
						.build();
		when(recipeService.getRecipeById(id)).thenReturn(Optional.of(dto));

		// when / then
		mockMvc.perform(get("/recipe/{id}", id))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.name").value("Curry"));
	}

	@Test
	void getRecipe_whenRecipeDoesNotExist_thenReturnsNotFound() throws Exception {
		// given
		final UUID id = UUID.randomUUID();
		when(recipeService.getRecipeById(id)).thenReturn(Optional.empty());

		// when / then
		mockMvc.perform(get("/recipe/{id}", id))
						.andExpect(status().isNotFound());
	}
}
