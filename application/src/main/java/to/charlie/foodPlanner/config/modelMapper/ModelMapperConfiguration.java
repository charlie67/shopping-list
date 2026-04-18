package to.charlie.foodPlanner.config.modelMapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedIngredientDto;
import to.charlie.foodPlanner.domain.model.dto.extraction.ExtractedRecipeDto;
import to.charlie.foodPlanner.domain.model.entity.recipe.RecipeEntity;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;

@Component
public class ModelMapperConfiguration {

  @Bean
  public ModelMapper modelMapper() {
    final ModelMapper modelMapper = new ModelMapper();

    modelMapper.addMappings(new PropertyMap<ExtractedRecipeIngredient, ExtractedIngredientDto>() {
      @Override
      protected void configure() {
        map().setIngredientName(source.getIngredientName());
        map().setUnit(source.getUnit());
        map().setFullName(source.getFullText());
      }
    });

    // use typeMap because these are just generic collections.
    modelMapper.typeMap(RecipeEntity.class, ExtractedRecipeDto.class)
        .addMapping(RecipeEntity::getSteps, ExtractedRecipeDto::setInstructions);

    return modelMapper;
  }
}
