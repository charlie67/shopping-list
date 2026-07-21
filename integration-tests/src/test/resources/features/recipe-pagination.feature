@RecipePagination
Feature: Recipe pagination and retrieval

  Scenario: Saved recipes are returned on the first page
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for any ingredient
    And I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    And "RESPONSE_STATUS" should be "200"
    And I send an HTTP POST request to "/recipe" with the previous response body
    And "RESPONSE_STATUS" should be "201"
    When I send an HTTP GET request to "/recipe?page=0"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | size             | 20                      |
      | number           | 0                       |
      | numberOfElements | 1                       |
      | totalElements    | 1                       |
      | totalPages       | 1                       |
      | first            | true                    |
      | last             | true                    |
      | content[0].id    | <valid_uuid>            |
      | content[0].name  | Chilli con carne recipe |

  Scenario: An out-of-range page returns no content
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for any ingredient
    And I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    And I send an HTTP POST request to "/recipe" with the previous response body
    When I send an HTTP GET request to "/recipe?page=1"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | number           | 1     |
      | numberOfElements | 0     |
      | totalElements    | 1     |
      | first            | false |
      | last             | true  |
