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

