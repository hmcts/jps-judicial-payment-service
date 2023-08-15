package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_service_record_id")
    private Long id;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "account_center_code")
    private String accountCenterCode;

    @Column(name = "onboarding_start_date")
    private LocalDate onboardingStartDate;

    @Column(name = "retention_time_in_months")
    private Integer retentionTimeInMonths;

    @Column(name = "close_recorded_record_after_time_in_months")
    private Integer closeRecordedRecordAfterTimeInMonths;
}
