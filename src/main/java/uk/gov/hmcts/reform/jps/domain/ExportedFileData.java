package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@org.hibernate.annotations.Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"exportedFile", "sittingRecord"})
@Entity
@Table(name = "exported_file_data")
public class ExportedFileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exported_file_data_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_file_id", nullable = false, updatable = false)
    private ExportedFile exportedFile;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sitting_record_id", nullable = false, updatable = false)
    private SittingRecord sittingRecord;

    @Column(name = "record_type")
    private String recordType;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "transaction_time")
    private LocalTime transactionTime;

    @Column(name = "pay_element_id")
    private Long payElementId;

    @Column(name = "pay_element_start_date")
    private LocalDate payElementStartDate;

    @Column(name = "fixed_or_temp_indicator")
    private String fixedOrTempIndicator;

    @Column(name = "employees_value")
    private String employeesValue;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "cost_center")
    private String costCenter;
}
