package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.List;
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
@Data
@ToString
@Entity
@Table(name = "judicial_office_holder")
public class JudicialOfficeHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_joh_record_id")
    private Long id;

    @Column(name = "personal_code")
    private String personalCode;

    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "judicialOfficeHolder",
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<JohPayroll> johPayrolls = new ArrayList<>();

    public void addJohPayroll(JohPayroll johPayroll) {
        this.johPayrolls.add(johPayroll);
        johPayroll.setJudicialOfficeHolder(this);
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
        return new HashCodeBuilder(17, 37).append(id).append(personalCode).append(johPayrolls).toHashCode();
    }
}
