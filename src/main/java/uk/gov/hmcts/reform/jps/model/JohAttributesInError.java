package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class JohAttributesInError {
    private String personalCode;
    private LocalDate sittingDate;
    private String email;
    private String postNominals;
    private String fullName;
}
