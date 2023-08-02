package uk.gov.hmcts.reform.jps;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AbstractTest {

    protected SittingRecord createSittingRecord(LocalDate sittingDate) {
        return SittingRecord.builder()
            .sittingDate(sittingDate)
            .statusId(StatusId.RECORDED.name())
            .regionId("1")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .build();
    }

    protected StatusHistory createStatusHistory(String statusId, String userId, String userName,
                                                SittingRecord sittingRecord) {
        return StatusHistory.builder()
            .statusId(statusId)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(userId)
            .changeByName(userName)
            .sittingRecord(sittingRecord)
            .build();
    }

}
