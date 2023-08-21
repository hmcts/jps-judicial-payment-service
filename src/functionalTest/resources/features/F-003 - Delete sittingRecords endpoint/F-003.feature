@F-003 @Functional
Feature: F-003 - Scenarios for the DELETE /sittingRecords endpoint

  @S-003.1 #AC01
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-recorder
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code

  @S-003.2 #AC02
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-submitter
    Given a user with the IDAM role of "jps-submitter"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code

  @S-003.3 @Ignore #AC03 Ignore until IJPS-62 is ready
  Scenario: Success response - Return 200 success with Sitting Record update status as deleted for jps-admin
    Given a user with the IDAM role of "jps-admin"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And the existing record is in Submitted state
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "positive" response is received with a "200 OK" status code

  @S-003.4 #AC04
  Scenario: Negative response - Return 403 Forbidden for jps-recorder when trying to delete a record created by another user
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-004_allFields"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-003.5 #AC05
  Scenario: Negative response - Return 403 Forbidden error if jps-publisher tries to access the endpoint
    Given a user with the IDAM role of "jps-publisher"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-003.6 @Functional #AC05
  Scenario: Negative response - Return 403 Forbidden error if jps-joh-admin tries to access the endpoint
    Given a user with the IDAM role of "jps-joh-admin"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code


  @S-003.7 @Ignore #AC06 Ignore until IJPS-62 is ready
  Scenario: Negative response - Return 409 Conflict for jps-recorder when trying to delete a record that is not in 'Recorded' status
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And the existing record is in Submitted state
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Conflict" status code

  @S-003.8 @Ignore #AC07 Ignore until IJPS-62 is ready
  Scenario: Negative response - Return 409 Conflict for jps-submitter when trying to delete a record that is not in 'Recorded' status
    Given a user with the IDAM role of "jps-submitter"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And the existing record is in Submitted state
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Conflict" status code

  @S-003.9 @Functional #AC08
  Scenario: Negative response - Return 409 Conflict for jps-admin when trying to delete a record that is not in 'Submitted' status
    Given a user with the IDAM role of "jps-admin"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "409 Conflict" status code
    And the response contains "errors[0]" as "Sitting Record Status ID is in wrong state"

  @S-003.10 #AC09
  Scenario: Negative response - Return 400 Bad Request when sittingRecordId is not passed
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as ""
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "sittingRecordId is mandatory"

  @S-003.11 #AC10
  Scenario: Negative response - Return 403 Forbidden when user doesn't have a valid role
    Given a user with the IDAM role of "ccd-import"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-003.12 #AC11
  Scenario: Negative response - Return 401 Unauthorised when the request is missing the s2s token
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-003.13
  Scenario: Negative response - Return 403 Forbidden when the request is passing an invalid service token
    Given a user with the IDAM role of "jps-recorder"
    And a record for the given hmctsServiceCode exists in the database with the payload "F-003_CreateRecord"
    And a search is done on the hmctsServiceCode "ABA5", with the payload "F-005_allFields" to get the "sittingRecords[0].sittingRecordId"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "sittingRecordId" as "id of the previously created record"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-003.14
  Scenario: Negative response - Return 404 Not Found when the sittingRecordId passed doesn't exist
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "sittingRecordId" as "100000"
    And a call is submitted to the "SittingRecord" endpoint using a "DELETE" request
    Then a "negative" response is received with a "404 Not Found" status code
