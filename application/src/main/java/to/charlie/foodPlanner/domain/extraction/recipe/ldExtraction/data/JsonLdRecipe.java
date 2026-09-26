package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * A schema.org Recipe node, held untyped throughout. Every JSON-LD property may legally arrive as a
 * scalar, a list, a {@code {"@value": ...}} wrapper or an {@code @id} reference, so a field typed
 * any more tightly than {@link JsonNode} is only a way for a valid page to fail to parse. The
 * reading is done by {@code JsonLdValues} instead.
 *
 * <p>Properties we do not use - author, publisher, hasPart, aggregateRating and the rest - are
 * deliberately absent rather than modelled and ignored, for the same reason.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLdRecipe {

	private JsonNode name;
	private JsonNode description;
	private JsonNode url;
	private JsonNode image;
	private JsonNode keywords;
	private JsonNode dateModified;
	private JsonNode datePublished;
	private JsonNode cookTime;
	private JsonNode prepTime;
	@JsonAlias("timeRequired")
	private JsonNode totalTime;
	private JsonNode recipeCategory;
	private JsonNode recipeCuisine; // todo map this all the way through
	/** {@code ingredients} is the pre-2015 spelling and is still emitted in the wild. */
	@JsonProperty("recipeIngredient")
	@JsonAlias({"ingredients", "supply"})
	private JsonNode recipeIngredients;
	/** Recipe is a subtype of HowTo, so some emitters use the base {@code step} property. */
	@JsonAlias({"step", "RecipeInstructions"})
	private JsonNode recipeInstructions;
	/** Likewise {@code yield} is the HowTo property {@code recipeYield} refines. */
	@JsonAlias("yield")
	private JsonNode recipeYield;
	private JsonNode nutrition;
}
