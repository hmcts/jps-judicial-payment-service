@F-001
Feature: F-001 - Scenarios for the GET /Fee endpoint

  @S-001.1 #AC01a
  Scenario: Successful response with content, when the request doesn't have any additional headers
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response has all the fields returned with correct values

  @S-001.2 #AC01b
  Scenario: Successful response with content, when the request contains judgeRoleTypeId and dateRange From and To
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request contains the additional header "judgeRoleTypeId" as "123"
    And the request contains the additional header "dateRangeFrom" as "123"
    And the request contains the additional header "dateRangeTo" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response has all the fields returned with correct values

  @S-001.3 #AC01c
  Scenario: Successful response with content, when the request contains judgeRoleTypeId
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request contains the additional header "judgeRoleTypeId" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response has all the fields returned with correct values

  @S-001.4 #AC01d
  Scenario: Successful response with content, when the request contains dateRange From and To
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1234"
    And the request contains the additional header "dateRangeFrom" as "123"
    And the request contains the additional header "dateRangeTo" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response has all the fields returned with correct values

  @S-001.5 #AC02a
  Scenario: Successful response with empty result set, when the request doesn't have any additional headers
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1236"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response is empty

  @S-001.6 #AC02b
  Scenario: Successful response with empty result set, when the request contains judgeRoleTypeId and dateRange From and To
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1236"
    And the request contains the additional header "judgeRoleTypeId" as "123"
    And the request contains the additional header "dateRangeFrom" as "123"
    And the request contains the additional header "dateRangeTo" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response is empty

  @S-001.7 #AC02c
  Scenario: Successful response with empty result set, when the request contains dateRange From and To
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1236"
    And the request contains the additional header "dateRangeFrom" as "123"
    And the request contains the additional header "dateRangeTo" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response is empty

  @S-001.8 #AC02d
  Scenario: Successful response with empty result set, when the request contains judgeRoleTypeId
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1236"
    And the request contains the additional header "judgeRoleTypeId" as "123"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
    And the response is empty

  @S-001.9 #AC03
  Scenario: Negative response, when the request doesn't have the hmctsServiceCode
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "blabla"

  @S-001.10 #AC04
  Scenario: Negative response, when the request user doesn't have permissions
    Given a user with the IDAM role of "caseworker"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1235"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "negative" response is received with a "401 Unauthorised" status code


  @S-001.11 #AC05
  Scenario: Negative response, when the service token is missing
    Given a user with the IDAM role of "jps-admin"
    When a request is missing the S2S token
    And the request contains the "hmctsServiceCode" as "1237"
    And a call is submitted to the "FeeEndpoint" endpoint using a "GET" request
    Then a "negative" response is received with a "403 Forbidden" status code
