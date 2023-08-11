package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "judicial_office_holder")
public class JudicialOfficeHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_joh_record_id")
    private Long id;

    @JoinColumn(name = "personal_code")
    private String personalCode;


    @OneToMany(mappedBy = "judicialOfficeHolder", orphanRemoval = true, cascade = {CascadeType.ALL})
    private final Set<JohAttributes> johAttributes = new HashSet<>();

    @Formula("""
        (select j.crown_servant_flag
        from joh_attributes j
        where j.local_joh_record_id = local_joh_record_id
        and j.effective_start_date <= current_date
        order by j.effective_start_date desc
        limit 1)
        """
    )
    private Boolean isActiveJohAttributesCrownFlag;

    public void addJohAttributes(JohAttributes johAttributes) {
        this.johAttributes.add(johAttributes);
        johAttributes.setJudicialOfficeHolder(this);
    }
}
