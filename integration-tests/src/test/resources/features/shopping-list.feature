@ShoppingList
Feature: Shopping list retrieval and manipulation

  Scenario: The user can add an item to the shopping list
    Given "itemName" is set to "Milk"
    When I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | title     | Milk         |
      | quantity  | 1            |
      | completed | false        |
      | id        | <valid_uuid> |