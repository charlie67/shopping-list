package to.charlie.foodPlanner.inrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.RecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedReceipeDto;
import to.charlie.foodPlanner.domain.service.RecipeService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService recipeService;

	@RequestMapping(value = "/recipes/extract", method = RequestMethod.POST)
	public ResponseEntity<ExtractedReceipeDto> extract(
					@RequestParam(value = "url") final String url) {
		try {
			return ResponseEntity.ok(recipeService.extractRecipeFromUrl(url));
		} catch (final IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = "/recipes/add")
	public ResponseEntity<RecipeDto> addNewRecipe(@RequestBody final RecipeDto recipe) {
		return ResponseEntity.ok(recipeService.saveNewRecipe(recipe));
	}
}
