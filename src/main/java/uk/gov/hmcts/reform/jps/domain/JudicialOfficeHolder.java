package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "judicial_office_holder")
public class JudicialOfficeHolder {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_joh_record_id")
    private Long id;

    @Column(name = "personal_code")
    private String personalCode;

    @ToString.Exclude
    @OneToMany(mappedBy = "judicialOfficeHolder",
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<JohPayroll> johPayrolls = new ArrayList<>();

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


    public void addJohPayroll(JohPayroll johPayroll) {
        this.johPayrolls.add(johPayroll);
        johPayroll.setJudicialOfficeHolder(this);
    }

    public void addJohAttributes(JohAttributes johAttributes) {
        this.johAttributes.add(johAttributes);
        johAttributes.setJudicialOfficeHolder(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof JudicialOfficeHolder that)) {
            return false;
        }

        return new EqualsBuilder().append(id, that.id).append(personalCode, that.personalCode).append(
            johPayrolls.size(),
            that.johPayrolls.size()
        ).isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this);
    }
}
