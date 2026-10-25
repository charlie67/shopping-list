@RecipeExtraction
Feature: Recipe parsing and retrieval

  Scenario: ld+json recipes are extracted and saved successfully
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | Chilli con carne recipe |
      | ingredients[0].fullText | 1 large onion           |
      | instructions[0].type    | HowToStep               |
    When I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should match the file: "recipes/my-best-chilli-response.json"

  Scenario: sectioned ld+json recipes keep their headings and every nested step
    Given recipe URL "/mock/recipes/howto-section" is set to return the data from file "recipes/howto-section.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    # The recipe sits in a bare top level array, lists its ingredients under the legacy "ingredients"
    # property, references its image by @id, and writes its times in plain words.
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/howto-section"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | Sectioned traybake recipe                          |
      | extractionMethod        | JSON-LD                                            |
      | imageUrl                | https://example.com/images/sectioned-traybake.jpg  |
      | prepTime                | PT10M                                              |
      | cookTime                | PT1H15M                                            |
      | ingredients[0].fullText | 1 large onion                                      |
      | instructions[0].type    | HowToSection                                       |
      | instructions[0].text    | Prepare the vegetables                             |
      | instructions[1].text    | Chop the onion and pepper.                         |
      | instructions[2].text    | Crush the garlic.                                  |
      | instructions[3].text    | Bake                                               |
      | instructions[4].text    | Toss everything in oil and bake for an hour.       |

  Scenario: a stub ld+json recipe falls through to the next extractor
    Given recipe URL "/mock/recipes/jsonld-stub-fallback" is set to return the data from file "recipes/jsonld-stub-fallback.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    # The page's only ld+json is a Recipe with nothing to cook, so returning it would stop the chain
    # before microdata ever saw the page.
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/jsonld-stub-fallback"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | Fallback veggie hash recipe |
      | extractionMethod        | microdata                   |
      | ingredients[0].fullText | 1 large onion               |

  Scenario: microdata recipes are extracted and saved successfully
    Given recipe URL "/mock/recipes/microdata-veggie-hash" is set to return the data from file "recipes/microdata-veggie-hash.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/microdata-veggie-hash"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | Microdata veggie hash recipe |
      | ingredients[0].fullText | 1 large onion                |
      | extractionMethod        | microdata                    |
    When I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should match the file: "recipes/microdata-veggie-hash-response.json"
    And I store the value of "id" from the HTTP response as "RECIPE_ID"
    When I send an HTTP GET request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should match the file: "recipes/microdata-veggie-hash-response.json"

  Scenario: JustTheRecipe recipes are extracted and saved successfully
    Given recipe URL "/mock/recipes/justtherecipe-fried-rice" is set to return the data from file "recipes/justtherecipe-fried-rice.html"
    And the JustTheRecipe service is set to return the data from file "just-the-recipe/fried-rice.json"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/justtherecipe-fried-rice"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | JustTheRecipe fried rice recipe |
      | ingredients[0].fullText | 1 large onion                   |
      | extractionMethod        | JustTheRecipe                   |
    When I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should match the file: "recipes/justtherecipe-fried-rice-response.json"
    And I store the value of "id" from the HTTP response as "RECIPE_ID"
    When I send an HTTP GET request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should match the file: "recipes/justtherecipe-fried-rice-response.json"

  Scenario: A requested extraction method is used instead of the first one that works
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And the JustTheRecipe service is set to return the data from file "just-the-recipe/fried-rice.json"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for any ingredient
    # This page has ld+json, so without the requested method it would never reach JustTheRecipe.
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli&extractionMethod=JustTheRecipe"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name             | JustTheRecipe fried rice recipe |
      | extractionMethod | JustTheRecipe                   |

  Scenario: A requested extraction method that fails does not fall back to another method
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And the JustTheRecipe service is set to return an error
    # The ld+json extractor would happily read this page, but it was not the method that was asked for.
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli&extractionMethod=JustTheRecipe"
    Then "RESPONSE_STATUS" should be "422"

  Scenario: An unknown extraction method is rejected
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli&extractionMethod=Telepathy"
    Then "RESPONSE_STATUS" should be "400"

  Scenario: Deleting a recipe leaves ingredients shared with other recipes intact
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And recipe URL "/mock/recipes/onion-soup" is set to return the data from file "recipes/onion-soup.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    # Both recipes list "1 large onion", so they end up pointing at the same row in the shared
    # ingredient table.
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    And I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And I store the value of "id" from the HTTP response as "RECIPE_ID"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/onion-soup"
    And I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And I store the value of "id" from the HTTP response as "SECOND_RECIPE_ID"
    When I send an HTTP DELETE request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    When I send an HTTP GET request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "404"
    # The surviving recipe still resolves its shared ingredients, so the delete did not take them.
    When I send an HTTP GET request to "/recipe/{SECOND_RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                    | Onion soup recipe |
      | ingredients[0].fullText | 1 large onion     |


  Scenario: Ingredients with a ranged quantity are extracted and saved with their upper bound
    Given recipe URL "/mock/recipes/slow-cooker-chicken" is set to return the data from file "recipes/slow-cooker-chicken.html"
    # The chicken is written the way BBC Food writes it: a decimal metric range followed by imperial
    # ranges. Only the first, metric, quantity is kept.
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/whole-chicken-range.json" for ingredient "1-1.2kg/2lb 4oz-2lb 10oz whole chicken"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/carrots-range.json" for ingredient "2-3 carrots"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/slow-cooker-chicken"
    Then "RESPONSE_STATUS" should be "200"
    # The onion is a single value, which the extractor sends with quantity_max equal to quantity, so
    # it is not treated as a range.
    And the response body should contain the following fields:
      | name                        | Slow cooker chicken recipe |
      | ingredients[0].quantity     | 1.0                        |
      | ingredients[0].quantityMax  | 1.2                        |
      | ingredients[0].quantityText | 1-1.2 kg                   |
      | ingredients[1].quantity     | 2.0                        |
      | ingredients[1].quantityMax  | 3.0                        |
      | ingredients[2].quantity     | 1.0                        |
      | ingredients[2].quantityMax  | <null>                     |
    When I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should match the file: "recipes/slow-cooker-chicken-response.json"
    And I store the value of "id" from the HTTP response as "RECIPE_ID"
    When I send an HTTP GET request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should match the file: "recipes/slow-cooker-chicken-response.json"

  Scenario: Editing a ranged quantity above its upper bound drops the range
    Given recipe URL "/mock/recipes/slow-cooker-chicken" is set to return the data from file "recipes/slow-cooker-chicken.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/whole-chicken-range.json" for ingredient "1-1.2kg/2lb 4oz-2lb 10oz whole chicken"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/carrots-range.json" for ingredient "2-3 carrots"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/slow-cooker-chicken"
    And I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And I store the value of "id" from the HTTP response as "RECIPE_ID"
    When I set "ingredients[0].quantity" in the previous response body to 1.5
    And I send an HTTP PATCH request to "/recipe/{RECIPE_ID}" with the previous response body
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | ingredients[0].quantity    | 1.5    |
      | ingredients[0].quantityMax | <null> |
      | ingredients[1].quantity    | 2.0    |
      | ingredients[1].quantityMax | 3.0    |
    When I send an HTTP GET request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | ingredients[0].quantity    | 1.5    |
      | ingredients[0].quantityMax | <null> |
      | ingredients[1].quantityMax | 3.0    |

  Scenario: An ingredient the breakdown service fails on is kept unparsed instead of failing the recipe
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return error status 500 for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name                          | Chilli con carne recipe |
      | ingredients[0].ingredientName | onion                   |
      | ingredients[0].quantity       | 1.0                     |
      | ingredients[1].ingredientName | 1 red pepper            |
      | ingredients[1].fullText       | 1 red pepper            |
      | ingredients[1].quantityText   | <null>                  |
      | ingredients[2].ingredientName | garlic                  |
      | ingredients[3].ingredientName | oil                     |
    When I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | ingredients[1].ingredientName | 1 red pepper |
      | ingredients[1].quantity       | 0.0          |
