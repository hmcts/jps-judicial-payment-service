package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.services.StatusHistoryService;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class EvaluateMatchingDuration implements DuplicateChecker {
    private final StatusHistoryService statusHistoryService;
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
        if (sittingRecordDuplicateCheckFields.getStatusId() == StatusId.RECORDED) {
            if (sittingRecordDuplicateCheckFields.getJudgeRoleTypeId()
                .equals(sittingRecordWrapper.getSittingRecordRequest().getJudgeRoleTypeId())) {
                sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            } else {
                SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();
                if (TRUE.equals(sittingRecordRequest.getReplaceDuplicate())) {
                    sittingRecordWrapper.setErrorCode(VALID);
                } else {
                    sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                }
            }

            if (sittingRecordWrapper.getErrorCode() != VALID) {
                statusHistoryService.updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
            }
        } else if (sittingRecordDuplicateCheckFields.getStatusId() != DELETED) {
            sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            statusHistoryService.updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
        }
    }
}
