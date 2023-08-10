package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Data
@ToString
@Entity
@Table(name = "judicial_office_holder")
public class JudicialOfficeHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_joh_record_id")
    private Long id;

    @JoinColumn(name = "personal_code")
    private String personalCode;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    private List<SittingRecord> sittingRecords;

    @ToString.Exclude
    @OneToMany(mappedBy = "judicialOfficeHolder", orphanRemoval = true, cascade = {CascadeType.ALL})
    private final List<JohAttributes> johAttributes = new ArrayList<>();

    @Formula("""
        (select j.crown_servant_flag
        from joh_attributes j
        where j.local_joh_record_id = local_joh_record_id
        and j.effective_start_date <= current_date
        order by j.effective_start_date desc
        limit 1)
        """
    )
    private Boolean getActiveJohAttributesCrownFlag;

    public void addSittingRecord(SittingRecord sittingRecord) {
        if (null == sittingRecords) {
            sittingRecords = new ArrayList<>();
        }
        this.sittingRecords.add(sittingRecord);
    }

    public void addJohAttributes(JohAttributes johAttributes) {
        this.johAttributes.add(johAttributes);
        johAttributes.setJudicialOfficeHolder(this);
    }

}
