package to.charlie.foodPlanner.domain.extraction.ingredient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.ingredientextractor.IngredientBreakdownDto;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.MeasurementUnit;
import to.charlie.foodPlanner.infrastructure.rest.clients.IngredientBreakdownClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientBreakdownService {
	private final IngredientBreakdownClient ingredientBreakdownClient;


	public List<ExtractedRecipeIngredient> convertIngredient(final String ingredientString) {
		final IngredientBreakdownDto dto;
		try {
			dto = ingredientBreakdownClient.extractIngredients(ingredientString);
		} catch (final RuntimeException e) {
			log.warn("Could not break down ingredient '{}', keeping it unparsed. Error: {}", ingredientString, e.getMessage());
			return List.of(unparsedIngredient(ingredientString));
		}

		if (dto == null || dto.ingredients() == null || dto.ingredients().isEmpty()) {
			log.warn("Ingredient breakdown found no ingredient in '{}', keeping it unparsed", ingredientString);
			return List.of(unparsedIngredient(ingredientString));
		}

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
								.quantityMax(rangeMax(quantity.quantity(), quantity.quantityMax()))
								.quantityText(quantity.text())
								.quantityUnit(quantity.unit())
								.unitText(quantity.unit())
								.unit(MeasurementUnit.convert(quantity.unit()));
			}

			ingredientList.add(builder.build());
		}

		return ingredientList;
	}

	private static ExtractedRecipeIngredient unparsedIngredient(final String ingredientString) {
		return ExtractedRecipeIngredient.builder()
						.ingredient(ExtractedIngredient.builder().name(ingredientString).build())
						.fullText(ingredientString)
						.build();
	}

	// The extractor sends quantity_max equal to quantity for a single value, so only keep it when it
	// actually describes a range.
	public static Double rangeMax(final Double quantity, final Double quantityMax) {
		if (quantity == null || quantityMax == null || quantityMax <= quantity) {
			return null;
		}
		return quantityMax;
	}
}
