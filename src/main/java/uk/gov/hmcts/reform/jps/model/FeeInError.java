package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FeeInError {
    private String hmctsServiceId;
    private String judgeRoleTypeId;
    private String judgeRoleTypeName;
    private LocalDate sittingDate;
}
