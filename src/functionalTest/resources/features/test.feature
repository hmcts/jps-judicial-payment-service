@feature
Feature: testing framework

  Scenario: Successful response when a valid user invokes the endpoint
    Given a user with the IDAM role of "jps-admin"
    When a request is prepared with appropriate values
    And a call is submitted to the "Test" endpoint using a "GET" request
    Then a "positive" response is received with a "200 OK" status code
