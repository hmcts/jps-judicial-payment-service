package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
@Table(name = "sitting_record")
public class SittingRecord {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sitting_record_ID")
    private Long id;

    @Column(name = "sitting_date")
    private LocalDate sittingDate;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "region_id")
    private String regionId;

    @Column(name =  "epimms_id")
    private String epimmsId;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    @Column(name = "personal_code", nullable = false)
    private String personalCode;

    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "judge_role_type_id")
    private String judgeRoleTypeId;

    private boolean am;

    private boolean pm;

    @ToString.Exclude
    @OneToMany(mappedBy = "sittingRecord",
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<StatusHistory> statusHistories = new ArrayList<>();

    public String getCreatedByUserId() {
        return getFirstStatusHistory()
            .map(StatusHistory::getChangedByUserId)
            .orElse(null);
    }

    public String getCreatedByUserName() {
        return getFirstStatusHistory()
            .map(StatusHistory::getChangedByName)
            .orElse(null);
    }

    public LocalDateTime getCreatedDateTime() {
        return getFirstStatusHistory()
            .map(StatusHistory::getChangedDateTime)
            .orElse(null);
    }

    public String getChangedByUserId() {
        return getLatestStatusHistory()
            .map(StatusHistory::getChangedByUserId)
            .orElse(null);
    }

    public String getChangedByUserName() {
        return getLatestStatusHistory()
            .map(StatusHistory::getChangedByName)
            .orElse(null);
    }

    public LocalDateTime getChangedByDateTime() {
        return getLatestStatusHistory()
            .map(StatusHistory::getChangedDateTime)
            .orElse(null);
    }

    public Optional<StatusHistory> getFirstStatusHistory() {
        return statusHistories.stream().min(Comparator.comparingLong(StatusHistory::getId));
    }

    public Optional<StatusHistory> getLatestStatusHistory() {
        return statusHistories.stream().max(Comparator.comparingLong(StatusHistory::getId));
    }

    public void addStatusHistory(StatusHistory statusHistory) {
        this.statusHistories.add(statusHistory);
        this.setStatusId(statusHistory.getStatusId());
        statusHistory.setSittingRecord(this);
    }


    public boolean equalsDomainObject(Object object) {
        if (object == null) {
            return false;
        }

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = (uk.gov.hmcts.reform.jps.domain.SittingRecord) object;

        return (sittingRecord.getId().equals(this.getId())
            && sittingRecord.isAm() == this.isAm()
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())
            && sittingRecord.getEpimmsId().equals(this.getEpimmsId())
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())
            && sittingRecord.isPm() == this.isPm()
            && sittingRecord.getRegionId().equals(this.getRegionId())
            && sittingRecord.getStatusId().equals(this.getStatusId())
            && (Objects.isNull(sittingRecord.getStatusHistories())
                && Objects.isNull(this.getStatusHistories())
                || (Objects.nonNull(sittingRecord.getStatusHistories())
                && Objects.nonNull(this.getStatusHistories())
                && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()
                && sittingRecord.getStatusHistories().containsAll(this.getStatusHistories()))));
    }
}
