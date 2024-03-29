package uk.gov.hmcts.reform.jps.model;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALID("valid"),
    INVALID_JUDGE_ROLE_TYPE_ID("invalidJudgeRoleTypeId"),
    DUPLICATE_RECORD("duplicateRecord"),
    INVALID_LOCATION("invalidLocation"),
    UN_KNOWN_JOH("unknownJoH"),
    POTENTIAL_DUPLICATE_RECORD("potentialDuplicateRecord"),
    INVALID_DUPLICATE_RECORD("invalidDuplicateRecord");

    final String value;

    ErrorCode(String value) {
        this.value = value;
    }
}
