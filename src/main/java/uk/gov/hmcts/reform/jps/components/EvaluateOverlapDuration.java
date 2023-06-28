package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.services.StatusHistoryService;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class EvaluateOverlapDuration implements DuplicateChecker {
    private final StatusHistoryService statusHistoryService;

    @Override
    public void next(DuplicateChecker duplicateChecker) {
        throw new UnsupportedOperationException();
    }

    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                             sittingRecordDuplicateCheckFields) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();

        if (((TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            && TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
            && (sittingRecordRequest.getDurationBoolean().getPm()
            || sittingRecordRequest.getDurationBoolean().getAm()))
            || ((sittingRecordRequest.getDurationBoolean().getPm()
            && sittingRecordRequest.getDurationBoolean().getAm())
            && (TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            || TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
            )) {

            if (sittingRecordDuplicateCheckFields.getStatusId() == RECORDED) {
                sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
            } else if (sittingRecordDuplicateCheckFields.getStatusId() != DELETED) {
                sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            }
            statusHistoryService.updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
        }
    }
}
