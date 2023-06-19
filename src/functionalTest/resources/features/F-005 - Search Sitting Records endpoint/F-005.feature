@F-005 @Functional
Feature: F-005 - Scenarios for the POST /searchSittingRecords endpoint

  @S-005.1 @Ignore #AC01
  Scenario: Success response when the request contains all the fields - Return 200 success with content
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the response returns the matching sitting records

  @S-005.2 @Ignore #AC02
  Scenario: Success response when the request contains only the mandatory fields - Return 200 success with content
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with only the mandatory fields" as in "S-005.2.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the response returns the matching sitting records

  @S-005.3 #AC03
  Scenario: Success response when the request contains all the fields - Return 200 success without content
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BFA1"
    And the request body contains the "payload with all the fields" as in "F-005_allFields.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordCount" is 0

  @S-005.4 #AC04
  Scenario: Negative response, when the request doesn't have the hmctsServiceCode
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as ""
    And the request body contains the "payload with all the fields" as in "F-005_allFields.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "hmctsServiceCode is mandatory"

  @S-005.5 #AC05
  Scenario: Negative response, when the request payload is missing pageSize
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing pageSize" as in "S-005.5.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Page size is mandatory"

  @S-005.6 #AC05
  Scenario: Negative response, when the request payload is missing offset
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing offset" as in "S-005.6.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Offset is mandatory"

  @S-005.7 #AC05
  Scenario: Negative response, when the request payload is missing regionId
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing regionId" as in "S-005.7.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Region Id is mandatory"

  @S-005.8 #AC05
  Scenario: Negative response, when the request payload is missing epimmsId
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing epimmsId" as in "S-005.8.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Epimms Id is mandatory"

  @S-005.9 #AC05
  Scenario: Negative response, when the request payload is missing dateOrder
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateOrder" as in "S-005.9.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date order is mandatory"

  @S-005.10 #AC05
  Scenario: Negative response, when the request payload is missing dateRange
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateRange" as in "S-005.10.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date range to is mandatory"
    And the response contains "errors[1].message" as "Date range from is mandatory"

  @S-005.11 @Ignore #AC06
  Scenario: Negative response, when the request user doesn't have permissions
    Given a user with the IDAM role of "ccd-admin"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-005.12 #AC07
  Scenario: Negative response, when the service token is invalid
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with all the fields" as in "F-005_allFields.json"
    And a call is submitted to the "SearchSittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code
