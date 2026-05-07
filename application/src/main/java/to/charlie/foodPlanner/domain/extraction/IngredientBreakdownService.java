package to.charlie.foodPlanner.domain.extraction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.infrastructure.rest.clients.IngredientBreakdownClient;

@Service
@RequiredArgsConstructor
public class IngredientBreakdownService {
	private final IngredientBreakdownClient ingredientBreakdownClient;


}
