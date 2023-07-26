package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
@Table(name = "sitting_record")
public class SittingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sitting_record_ID")
    private Long id;

    @Column(name = "sitting_date")
    private LocalDate sittingDate;

    @Column(name = "status_id")
    @Enumerated(EnumType.STRING)
    private StatusId statusId;

    @Column(name = "region_id")
    private String regionId;

    @Column(name =  "epimms_id")
    private String epimmsId;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    @Column(name = "personal_code")
    private String personalCode;

    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "judge_role_type_id")
    private String judgeRoleTypeId;

    private boolean am;

    private boolean pm;

    @ToString.Exclude
    @OneToMany(mappedBy = "sittingRecord",
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private final List<StatusHistory> statusHistories = new ArrayList<>();

    public String getCreatedByUserId() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByUserId() : null;
    }

    public String getCreatedByUserName() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByName() : null;
    }

    public LocalDateTime getCreatedDateTime() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? statusHistory.getChangeDateTime() : null;
    }

    public String getChangeByUserId() {
        StatusHistory statusHistory = getLatestStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByUserId() : null;
    }

    public String getChangeByUserName() {
        StatusHistory statusHistory = getLatestStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByName() : null;
    }

    public LocalDateTime getChangeByDateTime() {
        StatusHistory statusHistory = getLatestStatusHistory();
        return null != statusHistory ? statusHistory.getChangeDateTime() : null;
    }

    public StatusHistory getFirstStatusHistory() {
        List<StatusHistory> statusHistoriesCopy = statusHistories.stream()
            .sorted(Comparator.comparingLong(StatusHistory::getId))
            .toList();
        Optional<StatusHistory> optStatHistory = statusHistoriesCopy.stream().findFirst();
        return optStatHistory.isPresent() ? optStatHistory.get() : null;
    }

    public StatusHistory getLatestStatusHistory() {
        List<StatusHistory> statusHistoriesCopy = statusHistories.stream()
            .sorted(Comparator.comparingLong(StatusHistory::getId).reversed())
            .toList();
        Optional<StatusHistory> optStatHistory = statusHistoriesCopy.stream().findFirst();
        return optStatHistory.isPresent() ? optStatHistory.get() : null;
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

        return (sittingRecord.getId() == this.getId()
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())
            && sittingRecord.getEpimmsId().equals(this.getEpimmsId())
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())
            && sittingRecord.getRegionId().equals(this.getRegionId())
            && sittingRecord.getStatusId().equals(this.getStatusId())
            && (null == sittingRecord.getStatusHistories() && null == this.getStatusHistories()
            || null != sittingRecord.getStatusHistories() && null != this.getStatusHistories()
            && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()));
    }
}
