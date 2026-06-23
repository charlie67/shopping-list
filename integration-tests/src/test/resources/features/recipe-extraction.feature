@RecipeExtraction
Feature: Recipe parsing and retrieval

  Scenario: ld+json recipes are extracted and saved successfully
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for ingredient "1 large onion"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/pepper.json" for ingredient "1 red pepper"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/garlic.json" for ingredient "2 garlic cloves"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/oil.json" for ingredient "1 tbsp oil"
    When I send an HTTP POST request to "/recipe/extract?url={wiremock-url}/mock/recipes/my-best-chilli&save=true"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should match the file: "recipes/my-best-chilli-response.json"

