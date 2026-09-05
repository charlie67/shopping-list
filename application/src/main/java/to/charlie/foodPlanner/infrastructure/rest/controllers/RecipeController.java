package to.charlie.foodPlanner.infrastructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractionMethod;
import to.charlie.foodPlanner.domain.service.RecipeService;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService recipeService;

	@GetMapping("/extract")
	public ResponseEntity<ExtractedRecipeDto> extract(
					@RequestParam final String url,
					@RequestParam(required = false) final String extractionMethod) {

		// Resolved before the extraction so an unknown method name is a bad request, rather than
		// falling into the IllegalArgumentException catch below and reading as a server fault.
		final ExtractionMethod method;
		try {
			method = extractionMethod == null ? null : ExtractionMethod.fromName(extractionMethod);
		} catch (final IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}

		try {
			return ResponseEntity.ok(recipeService.extractRecipeFromUrl(url, method));
		} catch (final IOException e) {
			return ResponseEntity.notFound().build();
		} catch (final RecipeExtractionFailed e) {
			// The requested method could not read the page, and nothing else was tried.
			return ResponseEntity.unprocessableEntity().build();
		} catch (final IllegalArgumentException e) {
			return ResponseEntity.internalServerError().build();
		} catch (final DuplicateRecipeException e) {
			return ResponseEntity.ok().build();
		}
	}

	@PostMapping
	public ResponseEntity<ExtractedRecipeDto> save(@RequestBody final ExtractedRecipeDto extractedRecipe) {
		if (extractedRecipe.getId() != null) {
			return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
		}
		return ResponseEntity.status(CREATED).body(recipeService.saveRecipe(extractedRecipe));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ExtractedRecipeDto> update(@PathVariable final UUID id, @RequestBody final ExtractedRecipeDto extractedRecipe) {
		return ResponseEntity.ok(recipeService.updateRecipe(id, extractedRecipe));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable final UUID id) {
		recipeService.deleteRecipeById(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<Page<ExtractedRecipeDto>> getRecipePage(@RequestParam final int page) {
		return ResponseEntity.ok(recipeService.getRecipePage(page));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ExtractedRecipeDto> getRecipe(@PathVariable final UUID id) {
		return recipeService.getRecipeById(id)
						.map(ResponseEntity::ok)
						.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
