package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Service {

    private String hmctsServiceId;


    private String serviceName;


    private String accountCenterCode;


    private LocalDate onboardingStartDate;


    private Integer retentionTimeInMonths;


    private Integer closeRecordedRecordAfterTimeInMonths;
}
