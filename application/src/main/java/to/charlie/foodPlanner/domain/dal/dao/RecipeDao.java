package to.charlie.foodPlanner.domain.dal.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.dal.repository.PageableRecipeRepository;
import to.charlie.foodPlanner.domain.dal.repository.RecipeRepository;
import to.charlie.foodPlanner.domain.model.converter.recipe.ExtractedRecipeToRecipeEntityConverter;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;

@Component
@RequiredArgsConstructor
public class RecipeDao {

  private final RecipeRepository recipeRepository;
  private final PageableRecipeRepository pageableRecipeRepository;
  private final ExtractedRecipeToRecipeEntityConverter extractedRecipeConverter;
  private final ModelMapper modelMapper;

  public RecipeEntity save(final RecipeEntity recipeEntity) {
    return recipeRepository.save(recipeEntity);
  }

  public List<ExtractedRecipeDto> findAll() {
    return StreamSupport.stream(recipeRepository.findAll()
            .spliterator(), false)
        .map(it -> modelMapper.map(it, ExtractedRecipeDto.class))
        .toList();
  }

  public RecipeEntity save(final ExtractedRecipe recipeDto) {
    final RecipeEntity recipeEntity = extractedRecipeConverter.convert(recipeDto);
    return save(recipeEntity);
  }

  public boolean existsByUrl(final String url) {
    return recipeRepository.existsByUrl(url);
  }

  public Page<ExtractedRecipeDto> findPage(final int page) {
    final Pageable pageable = PageRequest.of(page, 20);

    final Page<RecipeEntity> allRecipesPageable = pageableRecipeRepository.findAll(pageable);

    return allRecipesPageable.map(it -> modelMapper.map(it, ExtractedRecipeDto.class));
  }

  public Optional<ExtractedRecipe> findByUrl(final String url) {
    return recipeRepository.findByUrl(url)
        .map(entity -> modelMapper.map(entity, ExtractedRecipe.class));
  }
}
