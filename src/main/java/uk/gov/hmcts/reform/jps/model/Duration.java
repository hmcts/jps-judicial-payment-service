package uk.gov.hmcts.reform.jps.model;

public enum Duration {
    AM("AM"),
    PM("PM"),
    FULL_DAY("Full day");

    final String value;

    Duration(String value) {
        this.value = value;
    }
}
