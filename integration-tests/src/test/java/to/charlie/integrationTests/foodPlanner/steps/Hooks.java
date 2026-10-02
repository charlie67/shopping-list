package to.charlie.integrationTests.foodPlanner.steps;

import io.cucumber.java.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Hooks {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@After
	public void resetDatabase() {
		// Reset the database after every scenario so each one starts from a clean state and
		// assertions on counts (e.g. pagination totals) stay deterministic. TRUNCATE ...
		// CASCADE clears the named tables and their dependents (recipe_ingredient, recipe_steps)
		// in one statement, avoiding JPA cascade-ordering issues with the recipe_ingredient FK.
		// cook_batch and planned_meal are listed even though recipe cascades to them, so a scenario that
		// plans without saving a recipe of its own still starts clean.
		jdbcTemplate.execute("TRUNCATE TABLE recipe, ingredient, shopping_list_item, tag, options,"
						+ " cook_batch, planned_meal RESTART IDENTITY CASCADE");
	}
}
