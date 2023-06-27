package uk.gov.hmcts.reform.jps.model.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SittingRecord {
    private long sittingRecordId;
    private LocalDate sittingDate;
    private String statusId;
    private String regionId;
    private String regionName;
    private String epimsId;
    private String hmctsServiceId;
    private String personalCode;
    private String personalName;
    private Long contractTypeId;
    private String judgeRoleTypeId;
    private String am;
    private String pm;
    @ToString.Exclude
    private List<StatusHistory> statusHistories;

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
        List<StatusHistory> statusHistoriesCopy = new ArrayList<>(statusHistories.stream().toList());
        Collections.sort(statusHistoriesCopy, Comparator.comparing(StatusHistory::getId));
        Optional<StatusHistory> optStatHistory = statusHistoriesCopy.stream().findFirst();
        return optStatHistory.isPresent() ? optStatHistory.get() : null;
    }

    public StatusHistory getLatestStatusHistory() {
        List<StatusHistory> statusHistoriesCopy = new ArrayList<>(statusHistories).stream().toList();
        if (null == statusHistories) {
            return null;
        } else {
            Collections.sort(statusHistoriesCopy,
                             (statusHistory1, statusHistory2) -> statusHistory2.getId().compareTo(
                                 statusHistory1.getId())
            );
            Optional<StatusHistory> optionalStatusHistory = statusHistoriesCopy.stream().findFirst();
            return optionalStatusHistory.orElse(null);
        }
    }

}
