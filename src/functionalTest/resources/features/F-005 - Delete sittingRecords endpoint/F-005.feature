@F-005
Feature: F-005 - Scenarios for the DELETE /sittingRecords endpoint

  @S-005.1 #AC01
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-recorder
    Given a sitting record is created
    And a user with the IDAM role of "jps-recorder"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code
    And the response is empty

  @S-005.2 #AC02
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-submitter
    Given a sitting record is created
    And a user with the IDAM role of "jps-submitter"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code

  @S-005.3 #AC03
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-admin
    Given a sitting record is created
    And a user with the IDAM role of "jps-admin"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code

  @S-005.4 #AC04
  Scenario: Negative response - Return 403 Forbidden for jps-recorder when trying to delete a record created by another user
    Given a sitting record is created by a different user
    And a user with the IDAM role of "jps-recorder"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-005.5 #AC05
  Scenario: Negative response - Return 409 Forbidden for jps-recorder when trying to delete a record that is not in 'Recorded' status
    Given a sitting record is created ans its status is not 'Recorded'
    And a user with the IDAM role of "jps-recorder"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Forbidden" status code

  @S-005.6 #AC06
  Scenario: Negative response - Return 409 Forbidden for jps-submitter when trying to delete a record that is not in 'Recorded' status
    Given a sitting record is created ans its status is not 'Recorded'
    And a user with the IDAM role of "jps-submitter"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Forbidden" status code

  @S-005.7 #AC07
  Scenario: Negative response - Return 409 Forbidden for jps-admin when trying to delete a record that is in 'Submitted' status
    Given a sitting record is created ans its status is 'Submitted'
    And a user with the IDAM role of "jps-admin"
    When the request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecordEndpoint" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Forbidden" status code

  @S-005.8 #AC08
  Scenario: Negative response - Return 400 Bad Request when sittingRecordId is not passed
    Given a user with the IDAM role of "jps-recorder"
    When the request is prepared with appropriate values
    And the request contains the "sittingRecordId" as ""
    And a call is submitted to the "SittingRecordsEndpoint" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code

  @S-005.9 #AC09
  Scenario: Negative response - Return 401 Unauthorised when user doesn't have a valid role
    Given a sitting record is created
    Given a user with the IDAM role of "ccd-importer"
    When the request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "1235"
    And the request contains the "sittingRecordId" as "id of the previously created record"
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-005.10 #AC10
  Scenario: Negative response - Return 403 Forbidden
    Given a sitting record is created
    Given a user with the IDAM role of "jps-recorder"
    When a request is missing the S2S token
    And the request contains the "hmctsServiceCode" as "1237"
    And the request contains the "sittingRecordId" as "id of the previously created record"
    Then a "negative" response is received with a "403 Forbidden" status code
