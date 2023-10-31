package uk.gov.hmcts.reform.jps.components;

import org.mockito.Mock;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.util.Random;

public class BasePublishSittingRecord {
    @Mock
    protected PublishErrorChecker publishErrorChecker;
    protected PublishErrors publishErrors;
    public static final String HMCTS_SERVICE_CODE = "BBA3";
    public static final String PERSONAL_CODE = "personalCode";
    public static final long CONTRACT_TYPE = 1L;
    public static final String JUDGE_ROLE_TYPE_ID = "judgeRoleTypeId";
    public static final String EMPIMMS_ID = "empimmsId";
    public static final LocalDate SITTING_DATE = LocalDate.now();

    protected void setup(PublishErrorChecker errorChecker) {
        errorChecker.next(publishErrorChecker);
        publishErrors = PublishErrors.builder().build();
    }

    protected SittingRecordPublishProjection.SittingRecordPublishFields getDefaultDbSittingRecord() {
        return  getDbSittingRecord(
            PERSONAL_CODE,
            CONTRACT_TYPE,
            JUDGE_ROLE_TYPE_ID,
            EMPIMMS_ID,
            LocalDate.now(),
            StatusId.SUBMITTED
        );
    }

    protected SittingRecordPublishProjection.SittingRecordPublishFields getDefaultDbSittingRecord(LocalDate now) {
        return  getDbSittingRecord(
            PERSONAL_CODE,
            CONTRACT_TYPE,
            JUDGE_ROLE_TYPE_ID,
            EMPIMMS_ID,
            now,
            StatusId.SUBMITTED
        );
    }

    protected SittingRecordPublishProjection.SittingRecordPublishFields getDbSittingRecord(String personalCode,
                                                                                           Long contractType,
                                                                                           String judgeRoleTypeId,
                                                                                           String empimmsId,
                                                                                           LocalDate sittingDate,
                                                                                           StatusId statusId) {
        return new SittingRecordPublishProjection.SittingRecordPublishFields() {
            @Override
            public Long getId() {
                return new Random().nextLong();
            }

            @Override
            public String getPersonalCode() {
                return personalCode;
            }

            @Override
            public Long getContractTypeId() {
                return contractType;
            }

            @Override
            public String getJudgeRoleTypeId() {
                return judgeRoleTypeId;
            }

            @Override
            public String getEpimmsId() {
                return empimmsId;
            }

            @Override
            public LocalDate getSittingDate() {
                return sittingDate;
            }

            @Override
            public StatusId getStatusId() {
                return statusId;
            }
        };
    }


}
