package uk.gov.hmcts.reform.jps.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;

@Component
public class EvaluateOverlapDuration implements DuplicateChecker {

    @Override
    public void next(DuplicateChecker duplicateChecker) {
        throw new UnsupportedOperationException();
    }

    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                             sittingRecordDuplicateCheckFields) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();

        if ((((TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            && TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
            && (sittingRecordRequest.getDurationBoolean().getPm()
            || sittingRecordRequest.getDurationBoolean().getAm()))
            || ((sittingRecordRequest.getDurationBoolean().getPm()
            && sittingRecordRequest.getDurationBoolean().getAm())
            && (TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            || TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
            ))) {
            sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
        }
    }
}

