@Options
Feature: Reading and updating options

  Scenario: Reading an option that has never been set returns its default value
    When I send an HTTP GET request to "/options/PLAN_PORTION_SIZE"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name  | PLAN_PORTION_SIZE |
      | value | 2                 |

  Scenario: An updated option is returned and persisted
    Given "optionValue" is set to "4"
    When I send an HTTP PUT request to "/options/PLAN_PORTION_SIZE" with the body from file: "update-option.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name  | PLAN_PORTION_SIZE |
      | value | 4                 |
    When I send an HTTP GET request to "/options/PLAN_PORTION_SIZE"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name  | PLAN_PORTION_SIZE |
      | value | 4                 |

  Scenario: Updating an option that is already set overwrites the previous value
    Given "optionValue" is set to "4"
    And I send an HTTP PUT request to "/options/PLAN_PORTION_SIZE" with the body from file: "update-option.json"
    And "optionValue" is set to "6"
    When I send an HTTP PUT request to "/options/PLAN_PORTION_SIZE" with the body from file: "update-option.json"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name  | PLAN_PORTION_SIZE |
      | value | 6                 |
    When I send an HTTP GET request to "/options/PLAN_PORTION_SIZE"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should contain the following fields:
      | name  | PLAN_PORTION_SIZE |
      | value | 6                 |

  Scenario: Reading an option that does not exist is rejected
    When I send an HTTP GET request to "/options/NOT_AN_OPTION"
    Then "RESPONSE_STATUS" should be "400"

  Scenario: Updating an option that does not exist is rejected
    Given "optionValue" is set to "true"
    When I send an HTTP PUT request to "/options/NOT_AN_OPTION" with the body from file: "update-option.json"
    Then "RESPONSE_STATUS" should be "400"
