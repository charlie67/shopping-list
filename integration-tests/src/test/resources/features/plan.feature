@Plan
Feature: Planning meals for a week

  # Every scenario starts from a real saved recipe, extracted through the usual flow against WireMock,
  # so the servings the planner divides is the one the parser actually read off the page.
  Background:
    Given recipe URL "/mock/recipes/my-best-chilli" is set to return the data from file "recipes/my-best-chilli.html"
    And ingredient breakdown service is set to return the data from file "ingredient-breakdown/onion.json" for any ingredient
    When I send an HTTP GET request to "/recipe/extract?url={WIREMOCK_URL}/mock/recipes/my-best-chilli"
    And I send an HTTP POST request to "/recipe" with the previous response body
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | recipeYield | 4 |
      | servings    | 4 |
    And I store the value of "id" from the HTTP response as "RECIPE_ID"

  Scenario: Planning a recipe books the cook night and places a leftover
    Given I am connected to the shopping list WebSocket
    # Serves 4, cooking for 2, so the pot makes two meals: Monday's cook and one leftover
    When I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | weekStart                       | 2026-09-21              |
      | weekEnd                         | 2026-09-27              |
      | cookingFor                      | 2                       |
      | batches[0].id                   | <valid_uuid>            |
      | batches[0].recipeName           | Chilli con carne recipe |
      | batches[0].recipeYield          | 4                       |
      | batches[0].servings             | 4                       |
      | batches[0].cookingFor           | 2                       |
      | batches[0].mealsTotal           | 2                       |
      | batches[0].spareMeals           | 0                       |
      | batches[0].meals[0].placement   | COOK                    |
      | batches[0].meals[0].plannedDate | 2026-09-21              |
      | batches[0].meals[1].placement   | LEFTOVER                |
      | batches[0].meals[1].plannedDate | 2026-09-23              |
    # The change is broadcast as the week it touched, not as the row that changed
    And I should receive a WebSocket message with the following fields:
      | messageType         | PLAN_UPDATED |
      | data.weekStarts[0]  | 2026-09-21   |
    And I store the value of "batches[0].meals[1].id" from the HTTP response as "LEFTOVER_ID"
    # Taking a meal off its day hands it back to the pool rather than deleting the batch, and the
    # DELETE answers with the whole week rather than no content
    When I send an HTTP DELETE request to "/plan/meal/{LEFTOVER_ID}?week=2026-09-21"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches[0].mealsTotal   | 2 |
      | batches[0].spareMeals   | 1 |
      | batches[0].meals.length() | 1 |

  Scenario: A leftover cannot be eaten before the night it is cooked
    When I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-leftover-before-the-cook.json"
    Then "RESPONSE_STATUS" should be "400"
    When I send an HTTP GET request to "/plan/week/2026-09-21"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches.length() | 0 |

  Scenario: Moving the cook night past its leftovers hands them back to the pool
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    And I store the value of "batches[0].meals[0].id" from the HTTP response as "COOK_ID"
    # Wednesday's leftover now falls on the night the pot is cooked, so it goes back to the pool
    And "MEAL_DATE" is set to "2026-09-23"
    When I send an HTTP PATCH request to "/plan/meal/{COOK_ID}?week=2026-09-21" with the body from file: "plan/move-cook-to.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches[0].mealsTotal           | 2          |
      | batches[0].spareMeals           | 1          |
      | batches[0].meals.length()       | 1          |
      | batches[0].meals[0].placement   | COOK       |
      | batches[0].meals[0].plannedDate | 2026-09-23 |

  Scenario: Removing the cook night puts the whole batch back in the queue
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    And I store the value of "batches[0].meals[0].id" from the HTTP response as "COOK_ID"
    When I send an HTTP DELETE request to "/plan/meal/{COOK_ID}?week=2026-09-21"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches.length()    | 0 |
      | queue.length()      | 1 |
      | queue[0].mealsTotal | 2 |
      | queue[0].spareMeals | 0 |

  Scenario: A queued batch reports no spare meals and accepts no leftovers
    When I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-queued.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | batches.length()      | 0 |
      | queue.length()        | 1 |
      | queue[0].mealsTotal     | 2 |
      | queue[0].spareMeals     | 0 |
      | queue[0].meals.length() | 0 |
    # queue[0].spareMeals is 0 because nothing can be placed before there is a cook night to follow:
    # counting its meals into the pool would advertise meals nothing can spend
    And I store the value of "queue[0].id" from the HTTP response as "BATCH_ID"
    And "MEAL_DATE" is set to "2026-09-24"
    When I send an HTTP POST request to "/plan/batch/{BATCH_ID}/meal?week=2026-09-21" with the body from file: "plan/place-leftover-on.json"
    Then "RESPONSE_STATUS" should be "400"
    # Giving it a day is choosing its cook night, which is allowed
    When I send an HTTP POST request to "/plan/batch/{BATCH_ID}/meal?week=2026-09-21" with the body from file: "plan/move-cook-to.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | queue.length()                  | 0          |
      | batches[0].meals[0].placement   | COOK       |
      | batches[0].meals[0].plannedDate | 2026-09-24 |
      | batches[0].spareMeals           | 1          |

  Scenario: A frozen portion is dateless and shows in the freezer with the night it was cooked
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    And I store the value of "batches[0].meals[1].id" from the HTTP response as "LEFTOVER_ID"
    When I send an HTTP PATCH request to "/plan/meal/{LEFTOVER_ID}?week=2026-09-21" with the body from file: "plan/freeze-meal.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | freezer.length()              | 1                       |
      | freezer[0].recipeName         | Chilli con carne recipe |
      | freezer[0].cookedOn           | 2026-09-21              |
      # The portion is reported twice on purpose, so the freezer panel renders without cross-referencing
      | batches[0].meals[1].placement | FROZEN                  |
      | batches[0].spareMeals         | 0                       |

  Scenario: A week that is not a Monday is snapped to its Monday rather than rejected
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    When I send an HTTP GET request to "/plan/week/2026-09-24"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | weekStart        | 2026-09-21 |
      | weekEnd          | 2026-09-27 |
      | batches.length() | 1          |
    When I send an HTTP GET request to "/plan/week/not-a-date"
    Then "RESPONSE_STATUS" should be "400"

  Scenario: Removing a batch takes its meals with it
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    And I store the value of "batches[0].id" from the HTTP response as "BATCH_ID"
    When I send an HTTP DELETE request to "/plan/batch/{BATCH_ID}?week=2026-09-21"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches.length() | 0 |
      | queue.length()   | 0 |
      | freezer.length() | 0 |

  Scenario: Deleting the recipe empties the nights it was planned on
    Given I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-monday.json"
    When I send an HTTP DELETE request to "/recipe/{RECIPE_ID}"
    Then "RESPONSE_STATUS" should be "200"
    # Silent by design: the row cascades in the database, so Monday just empties
    When I send an HTTP GET request to "/plan/week/2026-09-21"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches.length() | 0 |

  Scenario: Raising the household hands the newest leftovers back, and leaves earlier weeks alone
    # The only behaviour here that depends on today's date, so its dates are relative to it
    Given "optionValue" is set to "1"
    And I send an HTTP PUT request to "/options/PLAN_PORTION_SIZE" with the body from file: "update-option.json"
    # A fortnight ago: four meals out of the pot, none of them placed beyond the cook night
    And "FORTNIGHT_AGO" is set to the date -14 days from today
    And "COOK_DATE" is set to the date -14 days from today
    And I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-cook-only.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | batches[0].mealsTotal | 4 |
      | batches[0].spareMeals | 3 |
    # This week: the same pot, with every one of its four meals given a home
    Given "COOK_DATE" is set to the date 0 days from today
    And "LEFTOVER_1" is set to the date 1 days from today
    And "LEFTOVER_2" is set to the date 2 days from today
    And "LEFTOVER_3" is set to the date 3 days from today
    And I send an HTTP POST request to "/plan" with the body from file: "plan/chilli-relative-week.json"
    Then "RESPONSE_STATUS" should be "201"
    And the response body should contain the following fields:
      | batches[0].mealsTotal     | 4 |
      | batches[0].spareMeals     | 0 |
      | batches[0].meals.length() | 4 |
    # Cooking for four now, so the same pot makes one meal and three rows have to go
    When "optionValue" is set to "4"
    And I send an HTTP PUT request to "/options/PLAN_PORTION_SIZE" with the body from file: "update-option.json"
    Then "RESPONSE_STATUS" should be "200"
    # Three rows shed, newest first, and never the cook night
    When I send an HTTP GET request to "/plan/week/{COOK_DATE}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | cookingFor                    | 4    |
      | batches.length()              | 1    |
      | batches[0].mealsTotal         | 1    |
      | batches[0].cookingFor         | 4    |
      | batches[0].meals.length()     | 1    |
      | batches[0].meals[0].placement | COOK |
      | batches[0].spareMeals         | 0    |
    # A fortnight ago is left exactly as it was: evaporating a meal you have already eaten is worse
    # than a stale number
    When I send an HTTP GET request to "/plan/week/{FORTNIGHT_AGO}"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | batches.length()      | 1 |
      | batches[0].mealsTotal | 4 |
      | batches[0].cookingFor | 1 |
