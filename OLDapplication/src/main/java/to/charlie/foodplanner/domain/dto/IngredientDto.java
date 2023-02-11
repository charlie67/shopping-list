package to.charlie.foodplanner.domain.dto;

import java.io.Serializable;


/**
 * A DTO for the {@link to.charlie.foodplanner.domain.entity.IngredientEntity} entity
 */
public record IngredientDto(String ingredient) implements Serializable
{
}