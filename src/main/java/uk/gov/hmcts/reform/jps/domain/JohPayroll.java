package uk.gov.hmcts.reform.jps.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@ToString
@Entity
@Table(name = "joh_payroll")
public class JohPayroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "joh_payroll_id")
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "local_joh_record_id")
    private JudicialOfficeHolder judicialOfficeHolder;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "judge_role_type_id")
    private String judgeRoleTypeId;

    @Column(name = "payroll_id")
    private String payrollId;
}
