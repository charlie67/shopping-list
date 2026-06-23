package to.charlie.foodPlanner.infrastructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.service.RecipeService;

import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService recipeService;

	@PostMapping("/extract")
	public ResponseEntity<ExtractedRecipeDto> extract(
					@RequestParam final String url, @RequestParam(name = "save") final boolean saveRecipe) {
		try {
			return ResponseEntity.status(CREATED).body(recipeService.extractRecipeFromUrl(url, saveRecipe));
		} catch (final IOException e) {
			return ResponseEntity.notFound().build();
		} catch (final IllegalArgumentException e) {
			return ResponseEntity.internalServerError().build();
		} catch (final DuplicateRecipeException e) {
			return ResponseEntity.ok().build();
		}
	}

	@GetMapping
	public ResponseEntity<Page<ExtractedRecipeDto>> getRecipePage(@RequestParam final int page) {
		return ResponseEntity.ok(recipeService.getRecipePage(page));
	}
}
