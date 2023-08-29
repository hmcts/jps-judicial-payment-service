package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Builder
@NoArgsConstructor()
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
    private final List<JohAttributes> johAttributes = new ArrayList<>();

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
        if (o instanceof JudicialOfficeHolder that) {
            return Objects.equals(id, that.id) && Objects.equals(personalCode, that.personalCode) && Objects.equals(
                johPayrolls, that.johPayrolls) && Objects.equals(johAttributes, that.johAttributes);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personalCode, johPayrolls, johAttributes);
    }
}
