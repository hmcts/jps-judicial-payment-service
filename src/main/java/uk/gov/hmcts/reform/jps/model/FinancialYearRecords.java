package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FinancialYearRecords {
    private String financialYear;
    private long publishedCount;
    private long submittedCount;
}
