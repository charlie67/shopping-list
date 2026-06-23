package to.charlie.foodPlanner.domain.extraction.ingredient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;
import to.charlie.foodPlanner.infrastructure.rest.clients.IngredientBreakdownClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientBreakdownService {
	private final IngredientBreakdownClient ingredientBreakdownClient;

	public List<ExtractedRecipeIngredient> convertIngredient(final String ingredientString) {
		final IngredientBreakdownDto dto = ingredientBreakdownClient.extractIngredients(ingredientString);

		final var ingredientList = new ArrayList<ExtractedRecipeIngredient>();

		for (int i = 0; i < dto.ingredients().size(); i++) {
			final IngredientBreakdownDto.IngredientItemDto ingredient = dto.ingredients().get(i);
			final IngredientBreakdownDto.QuantityItemDto quantity =
							(dto.quantities() != null && i < dto.quantities().size()) ? dto.quantities().get(i) : null;

			final ExtractedRecipeIngredient.ExtractedRecipeIngredientBuilder builder =
							ExtractedRecipeIngredient.builder()
											.ingredient(ExtractedIngredient.builder().name(ingredient.name()).build())
											.comment(dto.comment())
											.purpose(dto.purpose())
											.fullText(ingredientString)
											.size(dto.size())
											.preparation(dto.preparation());

			if (quantity != null) {
				builder.quantity(quantity.quantity())
								.quantityText(quantity.text())
								.quantityUnit(quantity.unit())
								.unitText(quantity.unit())
								.unit(MeasurementUnit.convert(quantity.unit()));
			}

			ingredientList.add(builder.build());
		}

		return ingredientList;
	}
}
