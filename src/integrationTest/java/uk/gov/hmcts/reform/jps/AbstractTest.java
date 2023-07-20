package uk.gov.hmcts.reform.jps;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AbstractTest {

    protected static final Long CONTRACT_TYPE_ID = 2L;
    protected static final String EPIMS_ID = "123";
    protected static final String JUDGE_ROLE_TYPE_ID = "HighCourt";
    protected static final String PERSONAL_CODE = "001";
    protected static final String REGION_ID = "1";
    protected static final String SSC_ID = "ssc_id";

    protected SittingRecord createSittingRecord(LocalDate sittingDate) {
        return SittingRecord.builder()
            .sittingDate(sittingDate)
            .statusId(StatusId.RECORDED.name())
            .regionId(REGION_ID)
            .epimsId(EPIMS_ID)
            .hmctsServiceId(SSC_ID)
            .personalCode(PERSONAL_CODE)
            .contractTypeId(CONTRACT_TYPE_ID)
            .am(true)
            .judgeRoleTypeId(JUDGE_ROLE_TYPE_ID)
            .build();
    }

    protected StatusHistory createStatusHistory(String statusId, String userId,
                                                String userName, SittingRecord sittingRecord) {
        return StatusHistory.builder()
            .statusId(statusId)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(userId)
            .changeByName(userName)
            .sittingRecord(sittingRecord)
            .build();
    }

}
