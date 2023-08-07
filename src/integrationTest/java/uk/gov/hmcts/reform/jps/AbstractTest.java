package uk.gov.hmcts.reform.jps;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AbstractTest {

    protected static final Long CONTRACT_TYPE_ID = 2L;
    protected static final String EPIMMS_ID = "123";
    protected static final String JUDGE_ROLE_TYPE_ID = "HighCourt";
    protected static final String REGION_ID = "1";
    protected static final String SSC_ID = "ssc_id";

    protected SittingRecord createSittingRecord(LocalDate sittingDate, String personalCode) {
        return SittingRecord.builder()
            .sittingDate(sittingDate)
            .statusId(StatusId.RECORDED.name())
            .regionId(REGION_ID)
            .epimmsId(EPIMMS_ID)
            .hmctsServiceId(SSC_ID)
            .personalCode(personalCode)
            .contractTypeId(CONTRACT_TYPE_ID)
            .am(true)
            .judgeRoleTypeId(JUDGE_ROLE_TYPE_ID)
            .build();
    }

    protected StatusHistory createStatusHistory(String statusId, String userId,
                                                String userName, SittingRecord sittingRecord) {
        return StatusHistory.builder()
            .statusId(statusId)
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(userId)
            .changedByName(userName)
            .sittingRecord(sittingRecord)
            .build();
    }

}
