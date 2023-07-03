package uk.gov.hmcts.reform.jps.model.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private String contractTypeId;
    private String judgeRoleTypeId;
    private String am;
    private String pm;
    private LocalDateTime createdDateTime;
    private String createdByUserId;
    private String createdByUserName;
    private LocalDateTime changeDateTime;
    private String changeByUserId;
    private String changeByUserName;
}


