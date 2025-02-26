@ShoppingList
Feature: Shopping list retrieval and manipulation

  Scenario: The user can add an item to the shopping list
    When I add the folowing items to the shopping list "milk", "bread", "apple juice"
    Then the response status code is 201
    And when I send an HTTP GET request to "/shoppinglist"
    Then the response status code is 200
    And the response body is:
      """
      [
        {
          "title": "Milk"
        }
      ]
      """