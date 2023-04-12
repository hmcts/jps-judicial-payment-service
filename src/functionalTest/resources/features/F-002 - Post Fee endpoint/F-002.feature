@F-002
Feature: F-002 - Scenarios for the POST /Fee endpoint

  @S-002.1 #AC01
  Scenario: Successful response, returning a new feeId in the response
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response has all the fields returned with correct values
    And the response contains a new feeId

  @S-002.2 #AC02
  Scenario: Negative response, when the request is missing feeDescription
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request body contains the "payload that is missing the feeDescription" as in "S-002.2.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "blabla"

  @S-002.3 #AC02
  Scenario: Negative response, when the request is missing judgeRoleTypeId
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request body contains the "payload that is missing the judgeRoleTypeId" as in "S-002.3.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "blabla"

  @S-002.4 #AC02
  Scenario: Negative response, when the request is missing standardFee
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request body contains the "payload that is missing the standardFee" as in "S-002.4.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "blabla"

  @S-002.5 #AC02
  Scenario: Negative response, when the request is missing effectiveFrom
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request body contains the "payload that is missing the effectiveFrom" as in "S-002.4.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "blabla"

  @S-002.6 #AC03
  Scenario: Negative response, when the request doesn't have the hmctsServiceCode
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "004 unknown hmctsServiceCode"

  @S-002.7 #AC04
  Scenario: Negative response, when the request contains an unknown judgeRoleTypeId
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "005 unknown judgeRoleTypeId"

  @S-002.8 #AC05
  Scenario: Negative response, when the request contains an effective date that matches existing fee record
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "001 effective date matches existing fee record"

  @S-002.9 #AC06
  Scenario: Negative response, when the request user doesn't have permissions
    Given a user with the IDAM role of "caseworker"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1235"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-001.11 #AC05
  Scenario: Negative response, when the service token is missing
    Given a user with the IDAM role of "jps-admin"
    When a request is missing the S2S token
    And the request contains the "hmctsServiceCode" as "1237"
    And the request body contains the "payload with all the fields" as in "S-002.1.json"
    And a call is submitted to the "FeeEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code
