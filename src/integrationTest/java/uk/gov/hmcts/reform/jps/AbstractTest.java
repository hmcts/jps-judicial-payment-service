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
    protected static final String PERSONAL_CODE = "001";
    protected static final String REGION_ID = "1";
    protected static final String SSC_ID = "ssc_id";

    protected SittingRecord createSittingRecord(LocalDate sittingDate) {
        return SittingRecord.builder()
            .sittingDate(sittingDate)
            .statusId(StatusId.RECORDED)
            .regionId("1")
            .epimmsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId(JUDGE_ROLE_TYPE_ID)
            .build();
    }

    protected StatusHistory createStatusHistory(StatusId statusId, String userId, String userName,
                                                SittingRecord sittingRecord) {
        return StatusHistory.builder()
            .statusId(statusId)
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(userId)
            .changedByName(userName)
            .sittingRecord(sittingRecord)
            .build();
    }

}
