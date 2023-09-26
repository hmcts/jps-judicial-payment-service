package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
@Data
@Entity
@Table(name = "fee")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_record_id")
    private Long id;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    @Column(name = "judge_role_type_id")
    private String judgeRoleId;

    @Column(name = "standard_fee")
    private BigDecimal standardFee;

    @Column(name = "higher_threshold_fee")
    private BigDecimal higherThresholdFee;

    @Column(name = "london_weighted_fee")
    private BigDecimal londonWeightedFee;

    @Column(name = "effective_start_date")
    private LocalDate effectiveFrom;

    @Column(name = "fee_created_date")
    private LocalDate feeCreatedDate;

}
