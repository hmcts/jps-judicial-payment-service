package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "joh_attributes")
public class JohAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "joh_attributes_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_joh_record_id")
    private JudicialOfficeHolder judicialOfficeHolder;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "crown_servant_flag")
    private boolean crownServantFlag;

    @Column(name = "london_flag")
    private boolean londonFlag;
}
