package to.charlie.foodPlanner.domain.extraction.recipe.ldExtraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import to.charlie.foodPlanner.config.ObjectMapperConfiguration;
import to.charlie.foodPlanner.domain.extraction.ingredient.IngredientBreakdownService;
import to.charlie.foodPlanner.domain.model.exception.RecipeExtractionFailed;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipe;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeIngredient;
import to.charlie.foodPlanner.domain.model.internal.recipeExtraction.ExtractedRecipeInstruction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JsonLdExtractorTest {

	@Mock
	private IngredientBreakdownService ingredientBreakdownService;

	private JsonLdExtractor extractor;

	@BeforeEach
	void setUp() {
		final ObjectMapper objectMapper = new ObjectMapperConfiguration().jsonLdObjectMapper();
		extractor = new JsonLdExtractor(objectMapper,
						new JsonLdRecipeBuilder(ingredientBreakdownService, new JsonLdInstructionReader()));

		when(ingredientBreakdownService.convertIngredient(anyString())).thenAnswer(
						invocation -> List.of(ExtractedRecipeIngredient.builder().build()));
	}

	@Test
	void extract_whenGraphHoldsANodeWithMultipleTypes_thenStillFindsTheRecipe()
					throws RecipeExtractionFailed, IOException {
		// given a graph whose author node has an "@type" of ["Person", "Organization"]
		final Document document = documentWithJsonLd("recipeData/jsonld/example4");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://vidarbergum.com/recipe/cheats-lagman-uyghur-style-lamb-with-noodles/");

		// then
		assertThat(recipe.getName()).isEqualTo("Cheat's lagman – Uyghur inspired lamb with noodles");
		assertThat(recipe.getRecipeYield()).isEqualTo("6");
		assertThat(recipe.getRecipeCategory()).isEqualTo("Main Course");
		assertThat(recipe.getTotalTime()).isEqualTo("PT40M");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(16);
		assertThat(recipe.getExtractedRecipeInstructions()).hasSize(6);
		assertThat(recipe.getImageUrl()).isEqualTo(
						"https://vidarbergum.com/wp-content/uploads/2022/02/lagman-cheat-uyghur-noodles-lamb-11.jpg");
	}

	@Test
	void extract_whenTheRecipeHasNoNutritionBlock_thenNutritionIsLeftEmpty()
					throws RecipeExtractionFailed, IOException {
		// given
		final Document document = documentWithJsonLd("recipeData/jsonld/example4");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://vidarbergum.com/recipe/cheats-lagman-uyghur-style-lamb-with-noodles/");

		// then
		assertThat(recipe.getCalories()).isNull();
		assertThat(recipe.getProteinContent()).isNull();
		assertThat(recipe.getKeywords()).isEmpty();
	}

	@Test
	void extract_whenKeywordsAreACommaSeparatedString_thenTheyAreSplit()
					throws RecipeExtractionFailed {
		// given
		final Document document = Jsoup.parse("""
						<html><head><script type="application/ld+json">
						{"@context":"https://schema.org","@type":["Recipe","NewsArticle"],"name":"Soup",
						"keywords":"soup, winter ,warming","recipeYield":"4 servings",
						"recipeIngredient":"1 onion","recipeInstructions":"Boil it"}
						</script></head></html>""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Soup");
		assertThat(recipe.getKeywords()).containsExactly("soup", "winter", "warming");
		assertThat(recipe.getRecipeYield()).isEqualTo("4 servings");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(1);
		assertThat(recipe.getImageUrl()).isNull();
	}

	@Test
	void extract_whenTheBlockIsATopLevelArrayOfNodes_thenTheRecipeIsStillFound()
					throws RecipeExtractionFailed {
		// given the shape a great many sites emit - sibling nodes in a bare array, no "@graph"
		final Document document = documentWithInlineJsonLd("""
						[{"@type":"WebSite","name":"Example"},
						 {"@type":"Recipe","name":"Array soup","recipeIngredient":["1 onion"],
						  "recipeInstructions":["Boil it"]}]""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Array soup");
		assertThat(recipe.getExtractedRecipeInstructions()).hasSize(1);
	}

	@Test
	void extract_whenTheRecipeIsInsideANestedGraph_thenItIsStillFound()
					throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@context":"https://schema.org","@graph":[
						  {"@type":"WebPage","@graph":[
						    {"@type":"Recipe","name":"Nested soup","recipeIngredient":["1 onion"],
						     "recipeInstructions":["Boil it"]}]}]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Nested soup");
	}

	@Test
	void extract_whenTheContextIsAnObjectAndTheTypeIsPrefixed_thenTheRecipeIsStillRead()
					throws RecipeExtractionFailed {
		// given a "@context" that is not a plain string, and a namespaced "@type"
		final Document document = documentWithInlineJsonLd("""
						{"@context":{"@vocab":"http://schema.org/"},"@type":"schema:Recipe","name":"Vocab soup",
						 "recipeIngredient":["1 onion"],"recipeInstructions":["Boil it"]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Vocab soup");
	}

	@Test
	void extract_whenInstructionsAreGroupedIntoSections_thenTheHeadingsAndTheirStepsAreKept()
					throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Sectioned soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":[
						   {"@type":"HowToSection","name":"For the base","itemListElement":[
						     {"@type":"HowToStep","text":"Chop the onion"},
						     {"@type":"HowToStep","text":"Fry it"}]},
						   {"@type":"HowToSection","name":"To finish","itemListElement":
						     {"@type":"HowToStep","text":"Season"}}]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then the headings survive as steps of their own, and no step is lost
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getType, ExtractedRecipeInstruction::getText)
						.containsExactly(
										tuple("HowToSection", "For the base"),
										tuple("HowToStep", "Chop the onion"),
										tuple("HowToStep", "Fry it"),
										tuple("HowToSection", "To finish"),
										tuple("HowToStep", "Season"));
	}

	@Test
	void extract_whenThereIsASingleInstructionObject_thenItBecomesOneStep()
					throws RecipeExtractionFailed {
		// given instructions that are one object rather than a list of them
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"One step soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":{"@type":"HowToStep","text":"Boil it"}}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then - previously this iterated the object's values and produced a step per field
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getText)
						.containsExactly("Boil it");
	}

	@Test
	void extract_whenAStepCarriesItsWordingOnName_thenThatIsUsed() throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Named step soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":[{"@type":"HowToStep","name":"Boil it"}]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getText)
						.containsExactly("Boil it");
	}

	@Test
	void extract_whenInstructionsAreOneStringOfHtml_thenEachListItemBecomesAStep()
					throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Html soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":"<ol><li>Chop the onion</li><li>Boil it</li></ol>"}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getText)
						.containsExactly("Chop the onion", "Boil it");
	}

	@Test
	void extract_whenInstructionsAreOneStringOfLines_thenEachLineBecomesAStep()
					throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Lined soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":"Chop the onion\\n\\nBoil it\\n"}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then blank lines are dropped rather than stored as empty steps
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getText)
						.containsExactly("Chop the onion", "Boil it");
	}

	@Test
	void extract_whenTheImageIsAnObjectWithOnlyAContentUrl_thenThatUrlIsUsed()
					throws RecipeExtractionFailed {
		// given an ImageObject with no "@type" and no "url" - previously "" at best, a crash at worst
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Pictured soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":["Boil it"],
						 "image":{"contentUrl":"https://example.com/soup.jpg","width":800}}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getImageUrl()).isEqualTo("https://example.com/soup.jpg");
	}

	@Test
	void extract_whenTheImageIsAReferenceToAnotherNode_thenItIsResolved()
					throws RecipeExtractionFailed {
		// given the WordPress shape, where the recipe points at an ImageObject elsewhere in the graph
		final Document document = documentWithInlineJsonLd("""
						{"@context":"https://schema.org","@graph":[
						  {"@type":"ImageObject","@id":"https://example.com/soup/#primaryimage",
						   "url":"https://example.com/soup.jpg"},
						  {"@type":"Recipe","name":"Referenced soup","recipeIngredient":["1 onion"],
						   "recipeInstructions":["Boil it"],
						   "image":{"@id":"https://example.com/soup/#primaryimage"}}]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getImageUrl()).isEqualTo("https://example.com/soup.jpg");
	}

	@Test
	void extract_whenTheRecipeUsesTheLegacyIngredientsProperty_thenTheyAreStillRead()
					throws RecipeExtractionFailed {
		// given "ingredients", the pre-2015 spelling that is still emitted
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Legacy soup","ingredients":["1 onion","1 tbsp oil"],
						 "recipeInstructions":["Boil it"]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(2);
	}

	@Test
	void extract_whenTwoRecipeNodesArePresent_thenTheFullerOneWins() throws RecipeExtractionFailed {
		// given a stub node ahead of the real recipe, as two competing plugins produce
		final Document document = documentWithInlineJsonLd("""
						[{"@type":"Recipe","name":"Stub soup"},
						 {"@type":"Recipe","name":"Real soup","recipeIngredient":["1 onion","1 tbsp oil"],
						  "recipeInstructions":["Chop","Boil"]}]""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Real soup");
	}

	@Test
	void extract_whenAnEarlierBlockIsUnreadable_thenALaterBlockIsStillUsed()
					throws RecipeExtractionFailed {
		// given
		final Document document = Jsoup.parse("""
						<html><head>
						<script type="application/ld+json">{"@type":"Recipe", not json at all }</script>
						<script type="application/ld+json; charset=utf-8">
						{"@type":"Recipe","name":"Late soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":["Boil it"]}
						</script></head></html>""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then the charset suffix on the script type does not hide the block either
		assertThat(recipe.getName()).isEqualTo("Late soup");
	}

	@Test
	void extract_whenTheOnlyRecipeNodeHasNothingToCook_thenExtractionFails() {
		// given a recipe stub, of the kind a page's breadcrumb or review block carries
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Empty soup","description":"Nothing here"}""");

		// when / then - failing lets the extraction chain fall through to the other extractors
		assertThatThrownBy(() -> extractor.extract(document, "https://example.com/soup"))
						.isInstanceOf(RecipeExtractionFailed.class)
						.hasMessageContaining("No usable recipe JSON-LD found");
	}

	@Test
	void extract_whenTimesAreNotIso8601_thenTheyAreNormalised() throws RecipeExtractionFailed {
		// given the plain wording sites use at least as often as a duration
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Timed soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":["Boil it"],
						 "prepTime":"1 hr 30 mins","cookTime":30,"totalTime":"2:00"}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then a bare number is read as minutes, the way the sites that omit the unit mean it
		assertThat(recipe.getPrepTime()).isEqualTo("PT1H30M");
		assertThat(recipe.getCookTime()).isEqualTo("PT30M");
		assertThat(recipe.getTotalTime()).isEqualTo("PT2H");
	}

	@Test
	void extract_whenTimeIsAlreadyADuration_thenItIsLeftAlone() throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Iso soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":["Boil it"],"totalTime":"PT35M","cookTime":"nonsense"}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then a value we cannot read is passed through rather than dropped
		assertThat(recipe.getTotalTime()).isEqualTo("PT35M");
		assertThat(recipe.getCookTime()).isEqualTo("nonsense");
	}

	@Test
	void extract_whenTextHoldsMarkupAndEntities_thenItIsCleanedUp() throws RecipeExtractionFailed {
		// given doubly encoded entities and a stray tag, both of which reach the page as written
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":"Grandma&amp;#8217;s soup","recipeIngredient":["1 onion"],
						 "recipeInstructions":[{"@type":"HowToStep","text":"Boil <b>gently</b>"}]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Grandma’s soup");
		assertThat(recipe.getExtractedRecipeInstructions())
						.extracting(ExtractedRecipeInstruction::getText)
						.containsExactly("Boil gently");
	}

	@Test
	void extract_whenValuesAreLanguageTaggedObjects_thenTheValueIsRead()
					throws RecipeExtractionFailed {
		// given
		final Document document = documentWithInlineJsonLd("""
						{"@type":"Recipe","name":{"@value":"Tagged soup","@language":"en"},
						 "recipeIngredient":["1 onion"],"recipeInstructions":["Boil it"]}""");

		// when
		final ExtractedRecipe recipe = extractor.extract(document, "https://example.com/soup");

		// then
		assertThat(recipe.getName()).isEqualTo("Tagged soup");
	}

	@Test
	void extract_whenTheNutritionBlockIsPresent_thenEveryNutrientIsRead()
					throws RecipeExtractionFailed, IOException {
		// given a flat BBC Good Food recipe with an ImageObject image and a hasPart element
		final Document document = documentWithJsonLd("recipeData/example1");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://www.bbcgoodfood.com/recipes/teriyaki-salmon-parcels");

		// then
		assertThat(recipe.getName()).isEqualTo("Teriyaki salmon parcels");
		assertThat(recipe.getRecipeYield()).isEqualTo("4");
		assertThat(recipe.getRecipeCategory()).isEqualTo("Main course");
		assertThat(recipe.getCalories()).isEqualTo("257 calories");
		assertThat(recipe.getFatContent()).isEqualTo("13 grams fat");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(10);
		assertThat(recipe.getExtractedRecipeInstructions()).hasSize(6);
		assertThat(recipe.getImageUrl()).startsWith("https://images.immediate.co.uk/");
	}

	@Test
	void extract_whenACategoryIsACommaSeparatedString_thenItIsKeptWhole()
					throws RecipeExtractionFailed, IOException {
		// given a recipe whose category reads "Dinner, Lunch, Supper"
		final Document document = documentWithJsonLd("recipeData/jsonld/example2");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://www.bbcgoodfood.com/recipes/stir-fry-chilli-beef-sweet-potato-jackets");

		// then unlike keywords, a category is not split - it is one value
		assertThat(recipe.getRecipeCategory()).isEqualTo("Dinner, Lunch, Supper");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(13);
		assertThat(recipe.getExtractedRecipeInstructions()).hasSize(3);
	}

	@Test
	void extract_whenTheGraphHoldsVideoAndReviewSiblings_thenTheRecipeIsStillRead()
					throws RecipeExtractionFailed, IOException {
		// given
		final Document document = documentWithJsonLd("recipeData/jsonld/example3");

		// when
		final ExtractedRecipe recipe = extractor.extract(document,
						"https://www.daringgourmet.com/homemade-teriyaki-sauce/");

		// then the first entry of a list valued category and yield is taken, as before
		assertThat(recipe.getName()).isEqualTo("BEST Teriyaki Sauce Recipe");
		assertThat(recipe.getRecipeYield()).isEqualTo("12");
		assertThat(recipe.getRecipeCategory()).isEqualTo("condiment");
		assertThat(recipe.getTotalTime()).isEqualTo("PT10M");
		assertThat(recipe.getKeywords()).containsExactly("Teriyaki Sauce");
		assertThat(recipe.getExtractedRecipeIngredients()).hasSize(9);
		assertThat(recipe.getImageUrl())
						.isEqualTo("https://www.daringgourmet.com/wp-content/uploads/2013/05/Teriyaki-Sauce-1-square.jpg");
	}

	private Document documentWithInlineJsonLd(final String jsonLd) {
		return Jsoup.parse(
						"<html><head><script type=\"application/ld+json\">" + jsonLd + "</script></head></html>");
	}

	private Document documentWithJsonLd(final String resource) throws IOException {
		try (final var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
			final String jsonLd = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

			return Jsoup.parse("<html><head><script type=\"application/ld+json\">" + jsonLd
							+ "</script></head></html>");
		}
	}
}
