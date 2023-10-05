package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "sitting_days")
public class SittingDays {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sitting_days_id")
    private Long id;

    @Column(name = "personal_code")
    private String personalCode;

    @Column(name = "judge_role_type_id")
    private String judgeRoleTypeId;

    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "sitting_count")
    private Long sittingCount;
}
