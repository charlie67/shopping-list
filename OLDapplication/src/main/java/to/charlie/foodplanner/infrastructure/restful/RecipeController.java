package to.charlie.foodplanner.infrastructure.restful;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import to.charlie.foodplanner.domain.dao.Ingredientrepository;
import to.charlie.foodplanner.domain.dao.RecipeRepository;
import to.charlie.foodplanner.domain.dao.TagRepository;
import to.charlie.foodplanner.domain.dto.RecipeDto;
import to.charlie.foodplanner.domain.entity.IngredientEntity;
import to.charlie.foodplanner.domain.entity.RecipeEntity;
import to.charlie.foodplanner.domain.entity.TagEntity;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/recipes", produces = "application/json")
public class RecipeController
{
  final TagRepository tagRepository;

  final Ingredientrepository ingredientrepository;

  final RecipeRepository recipeRepository;

  @PostMapping("")
  public ResponseEntity<String> get(@RequestBody RecipeDto recipe)
  {
    Optional<TagEntity> optionalTag = tagRepository.findByName("1M+ Dataset");

    TagEntity tagEntity;
    if (optionalTag.isEmpty())
    {
      tagEntity = TagEntity.builder().name("1M+ Dataset").build();
      tagEntity = tagRepository.save(tagEntity);
    }
    else
    {
      tagEntity = optionalTag.get();
    }

    Set<IngredientEntity> ingredients = new HashSet<>();
    for (String ingredient : recipe.ingredients())
    {
      Optional<IngredientEntity> optionalIngredient = ingredientrepository.findByIngredient(ingredient);

      if (optionalIngredient.isPresent())
      {
        ingredients.add(optionalIngredient.get());
      }
      else
      {
        IngredientEntity ingredientEntity = IngredientEntity.builder().ingredient(ingredient).build();
        ingredientEntity = ingredientrepository.save(ingredientEntity);
        ingredients.add(ingredientEntity);
      }
    }

    String result = String.join("\n", recipe.instructions());

    RecipeEntity recipeEntity = RecipeEntity.builder()
        .title(recipe.title())
        .url(recipe.url())
        .tags(Set.of(tagEntity))
        .instructions(result)
        .ingredients(ingredients)
        .build();

    recipeRepository.save(recipeEntity);

    return ResponseEntity.ok("Done");
  }

}
