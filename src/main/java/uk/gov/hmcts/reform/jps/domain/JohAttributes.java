package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
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
@Getter
@Setter
@Entity
@Table(name = "joh_attributes")
public class JohAttributes {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "joh_attributes_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "local_joh_record_id")
    private JudicialOfficeHolder judicialOfficeHolder;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "crown_servant_flag")
    private boolean crownServantFlag;

    @Column(name = "london_flag")
    private boolean londonFlag;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof JohAttributes that) {
            return crownServantFlag == that.crownServantFlag && londonFlag == that.londonFlag && Objects.equals(
                id, that.id) && Objects.equals(judicialOfficeHolder, that.judicialOfficeHolder) && Objects.equals(
                effectiveStartDate, that.effectiveStartDate);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, judicialOfficeHolder, effectiveStartDate, crownServantFlag, londonFlag);
    }
}
