package to.charlie.foodPlanner.domain.dal.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.repository.RecipeRepository;
import to.charlie.foodPlanner.domain.model.converter.recipe.ExtractedRecipeToRecipeEntityConverter;
import to.charlie.foodPlanner.domain.model.dto.recipe.RecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;

import java.util.List;
import java.util.stream.StreamSupport;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
@RequiredArgsConstructor
public class RecipeDao {

  private final RecipeRepository recipeRepository;
  private final Converter<RecipeEntity, RecipeDto> converter;
  private final ExtractedRecipeToRecipeEntityConverter extractedRecipeConverter;

  public RecipeEntity save(final RecipeEntity recipeEntity) {
    return recipeRepository.save(recipeEntity);
  }

  public List<RecipeDto> findAll() {
    return StreamSupport.stream(recipeRepository.findAll()
                                                .spliterator(), false)
        .map(converter::convert)
        .toList();
  }

  public void save(ExtractedRecipe recipeDto) {
    RecipeEntity recipeEntity = extractedRecipeConverter.convert(recipeDto);
    recipeRepository.save(recipeEntity);
  }

  public boolean existsByUrl(String url) {
    return recipeRepository.existsByUrl(url);
  }
}
