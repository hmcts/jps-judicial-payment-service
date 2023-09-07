package uk.gov.hmcts.reform.jps.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@Component
public class EvaluateMatchingDuration implements DuplicateChecker {

    private DuplicateChecker duplicateChecker;

    @Override
    public void next(DuplicateChecker duplicateChecker) {
        this.duplicateChecker = duplicateChecker;
    }

    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                             sittingRecordDuplicateCheckFields) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();
        if (sittingRecordDuplicateCheckFields.getPm()
            .equals(sittingRecordRequest.getDurationBoolean().getPm())
            && sittingRecordDuplicateCheckFields.getAm()
            .equals(sittingRecordRequest.getDurationBoolean().getAm())) {
            checkRecordedSittingRecords(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
        } else {
            duplicateChecker.evaluate(sittingRecordWrapper,
                                      sittingRecordDuplicateCheckFields);
        }
    }

    private void checkRecordedSittingRecords(SittingRecordWrapper sittingRecordWrapper,
                                             SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                                                 sittingRecordDuplicateCheckFields) {
        if (sittingRecordDuplicateCheckFields.getStatusId() == RECORDED) {
            if (!sittingRecordDuplicateCheckFields.getEpimmsId()
                .equals(sittingRecordWrapper.getSittingRecordRequest().getEpimmsId())) {
                sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
            } else if (sittingRecordDuplicateCheckFields.getJudgeRoleTypeId()
                .equals(sittingRecordWrapper.getSittingRecordRequest().getJudgeRoleTypeId())) {
                sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            } else {
                sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
            }
        } else if (sittingRecordDuplicateCheckFields.getStatusId() != DELETED) {
            sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
        }
    }
}
