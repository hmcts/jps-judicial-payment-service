@F-006 @Functional
Feature: F-006 - Scenarios for the POST /submitSittingRecords endpoint

  @S-006.1 #AC02
  Scenario: Success response - Return 200 success with content of record submitted
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-003_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsSubmitted" is 1
    And the "recordsClosed" is 0

  @S-006.2 #AC03
  Scenario: Negative response - Return 403 Forbidden, valid JPS role but invalid role for this endpoint
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-006.3 #AC04
  Scenario: Negative response, return 403 Forbidden when the user has an invalid role for JPS
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-006.4 #AC05
  Scenario: Negative response, return 401 Unauthorised when the request is missing the service token
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-006.5
  Scenario: Negative response, return 403 Forbidden when the request uses an invalid service token
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-006.6
  Scenario: Negative response, when the request doesn't have the hmctsServiceCode
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as ""
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "hmctsServiceCode is mandatory"

  @S-006.7
  Scenario: Negative response, when the request payload is missing regionId
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing regionId" as in "S-006.7"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Region Id is mandatory"

  @S-006.8
  Scenario: Negative response, when the request payload is missing dateRangeFrom
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateRangeFrom" as in "S-006.8"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date range from is mandatory"

  @S-006.9
  Scenario: Negative response, when the request payload is missing dateRangeTo
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing dateRangeTo" as in "S-006.9"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Date range to is mandatory"

  @S-006.10
  Scenario: Negative response, when the request payload is missing submittedByIdamId
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing submittedByIdamId" as in "S-006.10"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Submitted by Idam Id is mandatory"

  @S-006.11
  Scenario: Negative response, when the request payload is missing submittedByName
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload missing submittedByName" as in "S-006.11"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Submitted by name is mandatory"

  @S-006.12 #AC01
  Scenario: Negative response, when the request has a hmctsServiceId passed that is not found (not in the service table)
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA4"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"

  @S-006.13 #AC02
  Scenario: Negative response, when the request has a hmctsServiceId passed but it is not yet on-boarded
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA3"
    And the request body contains the "payload with all the fields" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"
  
  @S-006.12 #AC01 IJPS-77
  Scenario: Success response - Return 200 success with recordsClosed value incremented by 1 when Contract_type_id=1
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.12_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 1
    And the "recordsSubmitted" is 0

  @S-006.13 #AC02 IJPS-77
  Scenario: Success response - Return 200 success with recordsClosed value incremented by 1 and recordsSubmitted value incremented by 1 when 1 record has Contract_type_id=2 and other has Contract_type_id=1
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.13_createRecords"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_generic"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 1
    And the "recordsSubmitted" is 1

  @S-006.14 #AC03 IJPS-77
  Scenario: Success response - Return 200 success with recordsClosed value incremented by 1 when record has Contract_type_id=6 and crown_servant_flag set to false
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.14_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_generic"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 1
    And the "recordsSubmitted" is 0

  @S-006.15 #AC04 IJPS-77
  Scenario: Success response - Return 200 success with recordsSubmitted value incremented by 1 when record has Contract_type_id=6 and crown_servant_flag set to true
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.15_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_allFields"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 0
    And the "recordsSubmitted" is 1

  @S-006.16 #AC05 IJPS-77
  Scenario: Success response - Return 200 success with no record updated when effective_start_date is before than sitting_date
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.16_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_generic"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 0
    And the "recordsSubmitted" is 0

  @S-006.17 #AC06 IJPS-77
  Scenario: Success response - Return 200 success with record is not updated when record has Contract_type_id=6 and there is no local_joh_record_id
    Given a user with the IDAM role of "jps-submitter"
    And a record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-006.17_createRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload matching data from existing record" as in "F-006_generic"
    And a call is submitted to the "SubmitSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "200 OK" status code
    And the "recordsClosed" is 0
    And the "recordsSubmitted" is 0
