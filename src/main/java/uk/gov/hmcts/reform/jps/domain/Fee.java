package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitting_record_id")
    private SittingRecord sittingRecordId;

    @Column(name = "fee_id")
    private String feeId;

    @Column(name = "judge_role_type_id")
    private String judgeRoleId;

    @Column(name = "standard_fee")
    private Integer standardFee;

    @Column(name = "london_weighted_fee")
    private Integer londonWeightedFee;

    @Column(name = "change_by_name")
    private String changeByName;

    @Column(name = "fee_description")
    private String feeDescription;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "pensionable_code")
    private Integer pensionableCode;
}
