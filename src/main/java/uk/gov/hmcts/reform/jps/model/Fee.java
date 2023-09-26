package uk.gov.hmcts.reform.jps.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Fee {
    private String hmctsServiceId;

    private String judgeRoleId;

    private BigDecimal standardFee;

    private BigDecimal higherThresholdFee;

    private BigDecimal londonWeightedFee;

    private LocalDate effectiveFrom;

    private LocalDate feeCreatedDate;
}
