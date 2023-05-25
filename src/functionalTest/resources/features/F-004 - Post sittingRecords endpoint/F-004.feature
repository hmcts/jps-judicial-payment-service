@F-004
Feature: F-004 - Scenarios for the POST /sittingRecords endpoint

  @S-004.1 #AC01
  Scenario: Success response - Return 201 success with content where errorCode to "valid" in response and ContractType is set to 2 in request
    Given a user with the IDAM role of "jps-recorder"
    And a record exists in fee table for the supplied hmctsServiceCode in the request along judgeRoleTypeId and sittingDate present in request where validFrom <= sittingDate
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "contractTypeId as 2" as in "S-004.1.json"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains "errorCode" as "valid"

  @S-004.2 #AC02
  Scenario: Success response - Return 201 success with content where errorCode to "valid" in response and ContractType is not set to 2 in request
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "contractTypeId is not set as 2" as in "S-004.2.json"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains "errorCode" as "valid"

  @S-004.3 #AC03 TBC

  @S-004.4 #AC04
  Scenario: Negative response - Return 400 - 008 could not insert with errorCode set to "invalidJudgeRoleTypeId" in response and ContractType is set to 2 in request
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "contractTypeId as 2 and an invalid invalidJudgeRoleTypeId" as in "S-004.4.json"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert with errorCode to invalidJudgeRoleTypeId for the sitting record"

  @S-004.5 #AC05 TBC

  @S-004.6 #AC06 TBC

  @S-004.7 #AC07
  Scenario: Negative response - Return 400 - 008 could not insert with errorCode set to "unknownJoH" in response and ContractType is set to 2 in request
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "contractTypeId as 2 and an invalid JoH" as in "S-004.7.json"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert with errorCode to unknownJoH for the sitting record"

  @S-004.8 #AC08 TBC

  @S-009.9 #AC09
  Scenario: Negative response - Return 400 - 004 unknown hmctsServiceCode
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "004 unknown hmctsServiceCode"

  @S-009.10 #AC10
  Scenario: Negative response - Return 400 - 006 Insufficient sitting Records
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "006 Insufficient sitting Records"

  @S-009.11 #AC11
  Scenario: Negative response - Return 400 Bad Request
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code

  @S-009.12 #AC12
  Scenario: Negative response - Return 401 Unauthorised
    Given a user with the IDAM role of "caseworker"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1235"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-001.11 #AC05
  Scenario: Negative response - Return 403 Forbidden
    Given a user with the IDAM role of "jps-recorder"
    When a request is missing the S2S token
    And the request contains the "hmctsServiceCode" as "1237"
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code
