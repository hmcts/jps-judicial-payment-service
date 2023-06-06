#@feature
#Feature: testing framework
#
#  Scenario: User with valid role submits request on /test POST endpoint
#    Given a user with the IDAM role of "jps-recorder"
#    When a request is prepared with appropriate values
#    And the request contains a valid service token
#    And a call is submitted to the "Test" endpoint using a "GET" request
#    Then a "positive" response is received with a "200 OK" status code
#
#  Scenario: User with invalid role submits request on /test POST endpoint
#    Given a user with the IDAM role of "ccd-import"
#    When a request is prepared with appropriate values
#    And the request contains a valid service token
#    And a call is submitted to the "Test" endpoint using a "GET" request
#    Then a "negative" response is received with a "401 Unauthorised" status code
#
#  Scenario: User with valid role but invalid S2S token submits request on /test POST endpoint
#    Given a user with the IDAM role of "jps-recorder"
#    When a request is prepared with appropriate values
#    And the request contains an invalid service token
#    And a call is submitted to the "Test" endpoint using a "GET" request
#    Then a "negative" response is received with a "403 Forbidden" status code
