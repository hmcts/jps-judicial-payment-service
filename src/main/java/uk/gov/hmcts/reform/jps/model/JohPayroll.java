package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class JohPayroll {
    private LocalDate effectiveStartDate;

    private String judgeRoleTypeId;

    private String payrollId;
}
