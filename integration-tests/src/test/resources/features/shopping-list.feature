@ShoppingList
Feature: Shopping list retrieval and manipulation

  Scenario: The user can add, complete, and delete an item from the shopping list
    Given I am connected to the shopping list WebSocket
    And "itemName" is set to "Milk"
    When I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | title     | Milk         |
      | quantity  | 1            |
      | completed | false        |
      | id        | <valid_uuid> |
    And I store the value of "id" from the HTTP response as "SHOPPING_LIST_ITEM_ID"
    # The creation is broadcast over the WebSocket
    And I should receive a WebSocket message with the following fields:
      | messageType    | SHOPPING_LIST_ITEM_CREATED |
      | data.title     | Milk                       |
      | data.completed | false                      |
      | data.id        | <valid_uuid>               |
    # Update the item name
    When "itemName" is set to "Chocolate Milk"
    And I send an HTTP PATCH request to "/shoppinglist/{shopping-id}" with the body from file: "update-name.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | title     | Chocolate Milk |
      | quantity  | 1              |
      | completed | false          |
      | id        | <valid_uuid>   |
    # The name change is broadcast over the WebSocket
    And I should receive a WebSocket message with the following fields:
      | messageType    | SHOPPING_LIST_ITEM_UPDATED |
      | data.title     | Chocolate Milk             |
      | data.completed | false                      |
      | data.id        | <valid_uuid>               |
    # Mark the item as completed
    When I send an HTTP PATCH request to "/shoppinglist/{shopping-id}" with the body from file: "complete-item.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | title     | Chocolate Milk |
      | quantity  | 1              |
      | completed | true           |
      | id        | <valid_uuid>   |
    # The completion is broadcast over the WebSocket
    And I should receive a WebSocket message with the following fields:
      | messageType    | SHOPPING_LIST_ITEM_UPDATED |
      | data.title     | Chocolate Milk             |
      | data.completed | true                       |
      | data.id        | <valid_uuid>               |
    # Delete the item
    When I send an HTTP DELETE request to "/shoppinglist/{shopping-id}"
    Then "RESPONSE_STATUS" should be "204"
    # The deletion is broadcast over the WebSocket
    And I should receive a WebSocket message with the following fields:
      | messageType | SHOPPING_LIST_ITEM_DELETED |
      | data.id     | <valid_uuid>               |

  Scenario: The shopping list can be read as a page with incomplete items first
    Given "itemName" is set to "Bread"
    And I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item.json"
    And "itemName" is set to "Eggs"
    And I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item.json"
    And I store the value of "id" from the HTTP response as "SHOPPING_LIST_ITEM_ID"
    And I send an HTTP PATCH request to "/shoppinglist/{shopping-id}" with the body from file: "complete-item.json"
    When I send an HTTP GET request to "/shoppinglist/pageable/0"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | totalElements        | 2     |
      | size                 | 100   |
      | number               | 0     |
      | numberOfElements     | 2     |
      | content[0].title     | Bread |
      | content[0].completed | false |
      | content[1].title     | Eggs  |
      | content[1].completed | true  |

  Scenario: Requesting a page beyond the shopping list returns no items
    Given I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item.json"
    When I send an HTTP GET request to "/shoppinglist/pageable/5"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | totalElements    | 1 |
      | number           | 5 |
      | numberOfElements | 0 |

  Scenario: Adding an item with an empty title is rejected
    When I send an HTTP POST request to "/shoppinglist" with the body from file: "add-item-empty-title.json"
    Then "RESPONSE_STATUS" should be "400"

  Scenario: Deleting an item that does not exist returns not found
    When I send an HTTP DELETE request to "/shoppinglist/00000000-0000-0000-0000-000000000000"
    Then "RESPONSE_STATUS" should be "404"
