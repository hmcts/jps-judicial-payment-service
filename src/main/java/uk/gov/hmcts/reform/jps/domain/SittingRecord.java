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

    public Optional<StatusHistory> getLatestStatusHistory() {

        Optional<StatusHistory> statusHistory = this.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangeDateTime));

        return statusHistory;
    }

    @ToString.Exclude
    @OneToMany(mappedBy = "sittingRecord",
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    private final List<StatusHistory> statusHistories = new ArrayList<>();

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "created_by_user_id")
    private String createdByUserId;

    @Column(name = "change_date_time")
    private LocalDateTime changeDateTime;

    @Column(name = "change_by_user_id")
    private String changeByUserId;

    public void addStatusHistory(StatusHistory statusHistory) {
        this.statusHistories.add(statusHistory);
        statusHistory.setSittingRecord(this);
    }

}
