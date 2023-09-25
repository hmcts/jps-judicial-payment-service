package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;
import uk.gov.hmcts.reform.jps.model.JohPayroll;

import java.util.List;

@Data
public class JohPayrollRequest {
    private Long judicialOfficeHolderId;
    private List<JohPayroll> johPayrolls;
}
