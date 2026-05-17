package to.charlie.foodPlanner.domain.extraction.ingredient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;
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

		IngredientBreakdownDto.QuantityItemDto lastQuantity = new IngredientBreakdownDto.QuantityItemDto(0D, "", "");

		for (int i = 0; i < dto.ingredients().size(); i++) {
			final IngredientBreakdownDto.IngredientItemDto ingredient = dto.ingredients().get(i);

			if (i < dto.quantities().size() && dto.quantities().get(i) != null) {
				lastQuantity = dto.quantities().get(i);
			}

			ingredientList.add(ExtractedRecipeIngredient.builder()
							.ingredientName(ingredient.name())
							.comment(dto.comment())
							.purpose(dto.purpose())
							.quantity(lastQuantity.quantity())
							.fullText(ingredientString)
							.quantityText(lastQuantity.text())
							.quantityUnit(lastQuantity.unit())
							.unitText(lastQuantity.unit())
							.unit(MeasurementUnit.convert(lastQuantity.unit()))
							.size(dto.size())
							.preparation(dto.preparation())
							.build());
		}

		return ingredientList;
	}
}
