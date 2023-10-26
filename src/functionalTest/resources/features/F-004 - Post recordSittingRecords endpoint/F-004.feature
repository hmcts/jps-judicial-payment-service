@F-004 @Functional
Feature: F-004 - Scenarios for the POST /recordSittingRecords endpoint

  @S-004.1 #AC01
  Scenario: Return 201 success with content where errorCode to "valid" in response when one sitting record is added
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"

  @S-004.2 #AC02
  Scenario: Return 201 success with content where errorCode to "valid" in response when multiple sitting records are added
    Given a user with the IDAM role of "jps-submitter"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with 3 sitting records" as in "S-004.2"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains 3 "errorRecords"
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].createdByName" as "Submitter"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[1].errorCode" as "VALID"
    And the response contains "errorRecords[1].createdByName" as "Submitter"
    And the response contains "errorRecords[1].statusId" as "RECORDED"
    And the response contains "errorRecords[2].errorCode" as "VALID"
    And the response contains "errorRecords[2].createdByName" as "Submitter"
    And the response contains "errorRecords[2].statusId" as "RECORDED"

  @S-004.3 #AC03
  Scenario: Negative response, return 400 when an invalid venue is supplied for the service
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BFA1"
    And the request body contains the "payload with an invalid epimmsId for BBA3" as in "S-004.3"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_LOCATION"

  @S-004.4 #AC04
  Scenario: Negative response, return 400 when an invalid hmctsServiceCode is passed
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "1234"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"

  @S-004.5 #AC05
  Scenario: Negative response, return 400 when request doesn't have any sitting records
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "BBA3"
    And the request body contains the "payload with empty recordedSittingRecords" as in "S-004.5"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "006 Insufficient sitting Records"

  @S-004.6 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'hmctsServiceCode'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as ""
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "hmctsServiceCode is mandatory"

  @S-004.7 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'recordedByIdamId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no hmctsServiceCode" as in "S-004.7"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Recorded By Idam Id is mandatory"

  @S-004.8 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'recordedByName'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no recordedByName" as in "S-004.8"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Recorded By Name is mandatory"

  @S-004.9 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'sittingDate'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no sittingDate" as in "S-004.9"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Sitting date is mandatory"

  @S-004.10 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'epimmsId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no epimmsId" as in "S-004.10"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Epimms Id is mandatory"

  @S-004.11 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'personalCode'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no personalCode" as in "S-004.11"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Personal code is mandatory"

  @S-004.12 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'contractTypeId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no contractTypeId" as in "S-004.12"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Contract Type Id  is mandatory"

  @S-004.13 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'judgeRoleTypeId'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no judgeRoleTypeId" as in "S-004.13"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "Judge Role Type Id is mandatory"

  @S-004.14 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'am'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no AM" as in "S-004.14"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "AM/PM/Full Day is mandatory"

  @S-004.15 #AC06
  Scenario: Negative response, return 400 Bad Request when the request is missing 'pm'
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with no PM" as in "S-004.15"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "AM/PM/Full Day is mandatory"

  @S-004.16 #AC07
  Scenario: Negative response, return 403 Forbidden, valid JPS role but invalid role for this endpoint
    Given a user with the IDAM role of "jps-publisher"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-004.17 #AC08
  Scenario: Negative response, return 403 Forbidden when the user has an invalid role
    Given a user with the IDAM role of "ccd-import"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-004.18 #AC08
  Scenario: Negative response, return 401 Unauthorised when the request is missing the service token
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "401 Unauthorised" status code

  @S-004.19 #AC09
  Scenario: Negative response, return 403 Forbidden when the request uses an invalid service token
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains an invalid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "403 Forbidden" status code

  @S-004.20 @PossibleDuplicates #AC02
  Scenario: Negative response, Return 400 - 008 could not insert with errorCode set to "potentialDuplicateRecord" in response when judgeRoleTypeId doesn't match existing record
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode, AM/PM matching the existing record, but judgeRoleTypeId does not match" as in "S-004.20"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "POTENTIAL_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.21 @PossibleDuplicates #AC03
  Scenario: Negative response, Return 400 - 008 could not insert with errorCode set to "invalidDuplicateRecord" in response when judgeRoleTypeId doesn't match and existing record is Submitted
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    And a call to submit the existing record with the payload "F-004_submitRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode, AM/PM matching the existing record, but judgeRoleTypeId does not match" as in "S-004.21"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "SUBMITTED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.22 @PossibleDuplicates #AC04
  Scenario: Negative response, Return 400 - 008 could not insert with errorCode set to "invalidDuplicateRecord" in response when existing record is Submitted
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    And a call to submit the existing record with the payload "F-004_submitRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode, AM/PM and judgeRoleTypeId matching the existing record" as in "S-004.22"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "SUBMITTED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.23 @PossibleDuplicates #AC05
  Scenario: Positive response, Return 201 - Success when period doesn't match with existing record
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode matching the existing record, but AM/PM does not match" as in "S-004.23"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 Created" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"

  @S-004.24 @PossibleDuplicates #AC06
  Scenario: Negative response, Return 400 - Invalid Location when one of the records has an invalid location for the given serviceCode
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-004.2"
    And a call to submit the existing record with the payload "F-004_submitRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "3 sitting records, 1 invalid due period intersection, 1 invalid due location and 1 invalid due existing one being Submitted" as in "S-004.24"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Submitter"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "false"
    And the response contains "errorRecords[0].pm" as "true"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Ramon Herrera"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"
    And the response contains "errorRecords[1].errorCode" as "INVALID_LOCATION"
    And the response contains "errorRecords[2].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[2].createdByName" as "Submitter"
    And the response contains "errorRecords[2].statusId" as "SUBMITTED"
    And the response contains "errorRecords[2].am" as "true"
    And the response contains "errorRecords[2].pm" as "false"
    And the response contains "errorRecords[2].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[2].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[2].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.25 @PossibleDuplicates #AC07
  Scenario: Negative response, Return 400 - 008 could not insert with multiple errors
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "3 sitting records, 1 potential due judgeRoleTypeId not matching, 1 invalid due matching everything and 1 valid" as in "S-004.25"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "POTENTIAL_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"
    And the response contains "errorRecords[1].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[1].createdByName" as "Recorder"
    And the response contains "errorRecords[1].am" as "true"
    And the response contains "errorRecords[1].pm" as "false"
    And the response contains "errorRecords[1].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[1].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[1].venue" as "Barnet Civil And Family Courts Centre"
    And the response contains "errorRecords[2].errorCode" as "VALID"

  @S-004.26 @PossibleDuplicates #AC09
  Scenario: Positive response - Return 201 for multiple records
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "3 sitting records, 2 valid, 1 potential with replaceDuplicate flag set to true" as in "S-004.26"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 OK" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[1].errorCode" as "VALID"
    And the response contains "errorRecords[1].statusId" as "RECORDED"
    And the response contains "errorRecords[1].createdByName" as "Recorder"
    And the response contains "errorRecords[2].errorCode" as "VALID"
    And the response contains "errorRecords[2].statusId" as "RECORDED"
    And the response contains "errorRecords[2].createdByName" as "Recorder"

  @S-004.27 @PossibleDuplicates #AC10
  Scenario: Negative response, Return 400 - 008 could not insert with errorCode set to "invalidDuplicateRecord" in response when AM/PM values intersect with existing record
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode matching the existing record, and AM/PM intersect" as in "S-004.27"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.28 @PossibleDuplicates #AC08
  Scenario: Negative response - Return 400 - 008 could not insert generic error
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "has personalCode with more characters than it is allowed by the db" as in "S-004.28"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "008 could not insert"

  @S-004.29 @PossibleDuplicates
  Scenario: When epimmsId is different from existing record - Return 400 - 008 could not insert with errorCode set to "potentialDuplicateRecord" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, judgeRoleTypeId, personalCode, AM/PM matching the existing record, but epimmsId does not match" as in "S-004.29"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "POTENTIAL_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.30 @PossibleDuplicates
  Scenario: When epimmsId is different and existing sitting record status is Submitted- Return 400 - 008 could not insert with errorCode set to "invalidDuplicate" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    And a call to submit the existing record with the payload "F-004_submitRecord"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, judgeRoleTypeId, personalCode, AM/PM matching the existing record, but epimmsId does not match" as in "S-004.30"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "SUBMITTED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.31 @PossibleDuplicates
  Scenario: When epimmsId and judgeRoleTypeId are different from existing record - Return 400 - 008 could not insert with errorCode set to "invalidDuplicate" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, personalCode, AM/PM matching the existing record, but epimmsId and judgeRoleTypeId do not match" as in "S-004.31"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "POTENTIAL_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.32 @PossibleDuplicates
  Scenario: When epimmsId and period are different from existing record - Positive response - Return 201 with errorCode set to "VALID" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, judgeRoleTypeId and personalCode matching the existing record, but epimmsId and AM/PM do not match" as in "S-004.32"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 OK" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].createdByName" as "Recorder"

  @S-004.33 @PossibleDuplicates
  Scenario: When epimmsId, judgeRoleTypeId and period are different from existing record - Positive response - Return 201 with errorCode set to "VALID" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate and personalCode matching the existing record, but epimmsId, judgeRoleTypeId and AM/PM do not match" as in "S-004.33"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "positive" response is received with a "201 OK" status code
    And the response contains "errorRecords[0].errorCode" as "VALID"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].createdByName" as "Recorder"

  @S-004.34 @PossibleDuplicates
  Scenario: When epimmsId is different from existing record and period intersects - Return 400 - 008 could not insert with errorCode set to "invalidDuplicate" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, judgeRoleTypeId and personalCode matching the existing record, but epimmsId does not match and AM/PM intersects" as in "S-004.34"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

  @S-004.35 @PossibleDuplicates
  Scenario: When epimmsId and judgeRoleTypeId are different from existing record and period intersects - Return 400 - 008 could not insert with errorCode set to "invalidDuplicate" in response
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "F-004_allFields"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate and personalCode matching the existing record, but epimmsId and judgeRoleTypeId do not match and AM/PM intersects" as in "S-004.35"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "INVALID_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "true"
    And the response contains "errorRecords[0].pm" as "false"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "Judge"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Bloggs"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"

@S-004.36 #AC01
  Scenario: Negative response, when the request has a hmctsServiceId passed that is not found (not in the service table)
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA4"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"

  @S-004.37 #AC02
  Scenario: Negative response, when the request has a hmctsServiceId passed but it is not yet on-boarded
    Given a user with the IDAM role of "jps-recorder"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA3"
    And the request body contains the "payload with one sitting record" as in "F-004_allFields"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "errors[0].message" as "004 unknown hmctsServiceCode"

  @S-004.38 @PossibleDuplicates #IJPS-124
  Scenario: Negative response, check whether correct record is returned - potential duplicate
    Given a user with the IDAM role of "jps-recorder"
    And "one" record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-004.38_existingRecord_1"
    And "a second" record for the hmctsServiceCode "ABA5" exists in the database with the payload "S-004.38_existingRecord_2"
    When a request is prepared with appropriate values
    And the request contains a valid service token
    And the request contains the "hmctsServiceCode" as "ABA5"
    And the request body contains the "sittingDate, epimmsId, personalCode, AM/PM matching existing record 1, but judgeRoleTypeId doesn't match" as in "S-004.38"
    And a call is submitted to the "RecordSittingRecords" endpoint using a "POST" request
    Then a "negative" response is received with a "400 Bad Request" status code
    And the response contains "message" as "008 could not insert"
    And the response contains "errorRecords[0].errorCode" as "POTENTIAL_DUPLICATE_RECORD"
    And the response contains "errorRecords[0].createdByName" as "Recorder"
    And the response contains "errorRecords[0].statusId" as "RECORDED"
    And the response contains "errorRecords[0].am" as "false"
    And the response contains "errorRecords[0].pm" as "true"
    And the response contains "errorRecords[0].judgeRoleTypeId" as "14"
    And the response contains "errorRecords[0].judgeRoleTypeName" as "Joe Ambrose"
    And the response contains "errorRecords[0].venue" as "Barnet Civil And Family Courts Centre"
