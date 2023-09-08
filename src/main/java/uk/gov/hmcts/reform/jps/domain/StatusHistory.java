package uk.gov.hmcts.reform.jps.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@org.hibernate.annotations.Immutable
@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_history_id")
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "sitting_record_id")
    private SittingRecord sittingRecord;

    @Column(name = "status_id")
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private StatusId statusId;

    @Column(name = "changed_date_time")
    private LocalDateTime changedDateTime;

    @Column(name = "changed_by_user_id")
    private String changedByUserId;

    @Column(name = "changed_by_name")
    private String changedByName;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StatusHistory that)) {
            return false;
        }

        return (that.getId().equals(this.getId())
            && that.getStatusId().equals(this.getStatusId())
            && that.getSittingRecord().getId().equals(this.getSittingRecord().getId())
            && that.getChangedByUserId().equals(this.getChangedByUserId())
            && that.getChangedDateTime().equals(this.getChangedDateTime()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(
            17,
            37
        ).append(id).append(sittingRecord).append(statusId).append(changedDateTime).append(changedByUserId).append(
            changedByName).toHashCode();
    }
}
