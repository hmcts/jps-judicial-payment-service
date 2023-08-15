@F-004 @Functional
Feature: F-004 - Scenarios for the POST /recordSittingRecords endpoint

  @S-004.1 #AC01
  Scenario: Return 201 success with content where errorCode to "valid" in response when one sitting record is added
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].createdByName" as "Recorder"

  @S-004.2 #AC02
  Scenario: Return 201 success with content where errorCode to "valid" in response when multiple sitting records are added
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with 3 sitting records" as in "S-004.2.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains 3 "errorRecords"
    And the response contains "errorRecords[2].errorCode" as "VALID"
    And the response contains "errorRecords[2].statusId" as "RECORDED"
    And the response contains "errorRecords[2].createdByName" as "Submitter"

  @S-004.3 #AC03
  Scenario: Negative response, return 400 when an invalid venue is supplied for the service
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with an invalid epimmsId for BBA3" as in "S-004.3.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "invalid location"

  @S-004.4 #AC04
  Scenario: Negative response, return 400 when an invalid hmctsServiceCode is passed
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"

  @S-004.5 #AC05
  Scenario: Negative response, return 400 when request doesn't have any sitting records
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with empty recordedSittingRecords" as in "S-004.5.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "006 Insufficient sitting Records"

  @S-004.6 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'hmctsServiceCode'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as ""
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "hmctsServiceCode is mandatory"

  @S-004.7 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'recordedByIdamId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no hmctsServiceCode" as in "S-004.7.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Recorded By Idam Id is mandatory"

  @S-004.8 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'recordedByName'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no recordedByName" as in "S-004.8.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Recorded By Name is mandatory"

  @S-004.9 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'sittingDate'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no sittingDate" as in "S-004.9.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Sitting date is mandatory"

  @S-004.10 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'epimmsId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no epimmsId" as in "S-004.10.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Epimms Id is mandatory"

  @S-004.11 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'personalCode'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no personalCode" as in "S-004.11.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Personal code is mandatory"

  @S-004.12 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'contractTypeId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no contractTypeId" as in "S-004.12.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Contract Type Id  is mandatory"

  @S-004.13 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'judgeRoleTypeId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no judgeRoleTypeId" as in "S-004.13.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Judge Role Type Id is mandatory"

  @S-004.14 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'am'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no AM" as in "S-004.14.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "AM/PM/Full Day is mandatory"

  @S-004.15 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'pm'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no PM" as in "S-004.15.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "AM/PM/Full Day is mandatory"

  @S-004.16 #AC07
  Scenario: Negative response, return 401 Unauthorised, valid JPS role but invalid role for this endpoint
    Given a user with the IDAM role of "jps-publisher"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-004.17 #AC08
  Scenario: Negative response, return 403 Forbidden when the user has an invalid role
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-004.18 #AC08
  Scenario: Negative response, return 401 Unauthorised when the request is missing the service token
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-004.19 #AC09
  Scenario: Negative response, return 403 Forbidden when the request uses an invalid service token
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields.json"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code
