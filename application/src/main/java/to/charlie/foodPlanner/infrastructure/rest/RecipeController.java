package to.charlie.foodPlanner.infrastructure.rest;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.recipe.RecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.exception.DuplicateRecipeException;
import to.charlie.foodPlanner.domain.service.RecipeService;

@RestController
@RequiredArgsConstructor
public class RecipeController {

  private final RecipeService recipeService;

  @RequestMapping(value = "/recipes/extract", method = RequestMethod.POST)
  public ResponseEntity<ExtractedRecipeDto> extract(
      @RequestParam(value = "url") final String url) {
    try {
      return ResponseEntity.ok(recipeService.extractRecipeFromUrl(url));
    } catch (final IOException e) {
      return ResponseEntity.notFound().build();
    } catch (final IllegalArgumentException e) {
      return ResponseEntity.internalServerError().build();
    } catch (final DuplicateRecipeException e) {
      return ResponseEntity.ok().build();
    }
  }

  @GetMapping("/recipes")
  public ResponseEntity<List<RecipeDto>> getAllRecipes() {
    return ResponseEntity.ok(recipeService.getAllRecipes());
  }
}
