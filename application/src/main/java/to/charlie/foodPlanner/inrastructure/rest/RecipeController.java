package to.charlie.foodPlanner.inrastructure.rest;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.RecipeDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedReceipeDto;
import to.charlie.foodPlanner.domain.service.RecipeService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RecipeController {

  private final RecipeService recipeService;

  @RequestMapping(value = "/recipes/add", method = RequestMethod.POST)
  public ResponseEntity<ExtractedReceipeDto> add(
      @RequestParam(value = "url", required = false) String url,
      @RequestBody(required = false) RecipeDto recipeDto) {
    try {
      return ResponseEntity.ok(recipeService.extractRecipeFromUrl(url));
    } catch (IOException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
