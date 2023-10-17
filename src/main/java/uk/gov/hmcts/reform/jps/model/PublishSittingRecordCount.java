package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublishSittingRecordCount {
    private FinancialYearRecords currentFinancialYear;
    private FinancialYearRecords previousFinancialYear;
}
