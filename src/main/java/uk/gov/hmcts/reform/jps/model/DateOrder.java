package uk.gov.hmcts.reform.jps.model;

public enum DateOrder {
    ASCENDING("ascending"),
    DESCENDING("descending");

    final String value;

    DateOrder(String value) {
        this.value = value;
    }
}
