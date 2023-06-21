package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private String statusId;

    @Column(name = "region_id")
    private String regionId;

    @Column(name =  "epims_id")
    private String epimsId;

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
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<StatusHistory> statusHistories = new ArrayList<>();

    public String getCreatedByUserId() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByUserId() : null;
    }

    public LocalDateTime getCreatedDateTime() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? getFirstStatusHistory().getChangeDateTime() : null;
    }

    public String getChangeByUserId() {
        StatusHistory statusHistory = getLatestStatusHistory();
        return null != statusHistory ? statusHistory.getChangeByUserId() : null;
    }

    public LocalDateTime getChangeDateTime() {
        StatusHistory statusHistory = getLatestStatusHistory();
        return null != statusHistory ? getLatestStatusHistory().getChangeDateTime() : null;
    }

    public StatusHistory getFirstStatusHistory() {
        Collections.sort(statusHistories, Comparator.comparing(StatusHistory::getId));
        Optional<StatusHistory> optStatHistory = statusHistories.stream().findFirst();
        return optStatHistory.isPresent() ? optStatHistory.get() : null;
    }

    public StatusHistory getLatestStatusHistory() {
        if (null == statusHistories) {
            return null;
        } else {
            Collections.sort(statusHistories,
                             (statusHistory1, statusHistory2) -> statusHistory2.getChangeDateTime().compareTo(
                                 statusHistory1.getChangeDateTime())
            );
            Optional<StatusHistory> optionalStatusHistory = statusHistories.stream().findFirst();
            return optionalStatusHistory.isPresent() ? optionalStatusHistory.get() : null;
        }
    }

    public void addStatusHistory(StatusHistory statusHistory) {
        this.statusHistories.add(statusHistory);
        statusHistory.setSittingRecord(this);
    }
}
