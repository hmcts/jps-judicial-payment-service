package uk.gov.hmcts.reform.jps.model;

import lombok.Getter;

@Getter
public enum JpsRole {
    ROLE_PUBLISHER("jps-publisher"),
    ROLE_SUBMITTER("jps-submitter"),
    ROLE_RECORDER("jps-recorder"),
    ROLE_ADMIN("jps-admin"),
    ROLE_CCD_IMPORT("ccd-import");

    final String value;

    JpsRole(String value) {
        this.value = value;
    }

}
