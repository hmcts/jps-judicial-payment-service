package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
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
@NoArgsConstructor()
@AllArgsConstructor
@Data
@ToString
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_history_id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sitting_record_id")
    private SittingRecord sittingRecord;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "change_date_time")
    private LocalDateTime changeDateTime;

    @Column(name = "change_by_user_id")
    private String changeByUserId;

    @Column(name = "change_by_name")
    private String changeByName;

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
            && that.getChangeByUserId().equals(this.getChangeByUserId())
            && that.getChangeDateTime().equals(this.getChangeDateTime()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(
            17,
            37
        ).append(id).append(sittingRecord).append(statusId).append(changeDateTime).append(changeByUserId).append(
            changeByName).toHashCode();
    }
}
