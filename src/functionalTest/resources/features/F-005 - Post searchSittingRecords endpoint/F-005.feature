@F-005 @Functional
Feature: F-005 - Scenarios for the POST /searchSittingRecords endpoint

  @S-005.1 #AC01
  Scenario: Success response when the request contains all the fields - Return 200 success with content
    Given a user with the IDAM role of "jps-recorder"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the response returns the matching sitting records

  @S-005.2 #AC02
  Scenario: Success response when the request contains only the mandatory fields - Return 200 success with content
    Given a user with the IDAM role of "jps-recorder"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "S-005.2"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the response returns the matching sitting records

  @S-005.3 #AC03
  Scenario: Success response when the request contains all the fields - Return 200 success without content
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BFA1"
    And the request body contains the "payload with all the fields" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordCount" is 0

  @S-005.4 #AC04
  Scenario: Negative response, when the request doesn't have the hmctsServiceCode
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as ""
    And the request body contains the "payload with all the fields" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "hmctsServiceCode is mandatory"

  @S-005.5 #AC05
  Scenario: Negative response, when the request payload is missing pageSize
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing pageSize" as in "S-005.5"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Page size is mandatory"

  @S-005.6 #AC05
  Scenario: Negative response, when the request payload is missing offset
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing offset" as in "S-005.6"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Offset is mandatory"

  @S-005.9 #AC05
  Scenario: Negative response, when the request payload is missing dateOrder
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateOrder" as in "S-005.9"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date order is mandatory"

  @S-005.10 #AC05
  Scenario: Negative response, when the request payload is missing dateRangeFrom
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateRangeFrom" as in "S-005.10"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date range from is mandatory"

  @S-005.11 #AC05
  Scenario: Negative response, when the request payload is missing dateRangeTo
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateRangeTo" as in "S-005.11"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date range to is mandatory"

  @S-005.12 #AC06
  Scenario: Negative response, return 403 Forbidden when the user has an invalid role for JPS
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-005.13 #AC07
  Scenario: Negative response, when the service token is invalid
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-005.14
  Scenario: Negative response, return 401 Unauthorised when the request is missing the service token
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields"
    And a call is submitted to the "SearchSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code
