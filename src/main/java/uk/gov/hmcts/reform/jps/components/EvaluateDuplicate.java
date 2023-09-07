package uk.gov.hmcts.reform.jps.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

@Component
public class EvaluateDuplicate implements DuplicateChecker {

    private DuplicateChecker duplicateChecker;

    @Override
    public void next(DuplicateChecker duplicateChecker) {
        this.duplicateChecker = duplicateChecker;
    }

    @Override
    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                            SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                                sittingRecordDuplicateCheckFields) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();
        if (sittingRecordDuplicateCheckFields.getSittingDate().isEqual(sittingRecordRequest.getSittingDate())
            && sittingRecordDuplicateCheckFields.getPersonalCode()
            .equals(sittingRecordRequest.getPersonalCode())) {
            sittingRecordWrapper.setSittingRecordId(sittingRecordDuplicateCheckFields.getId());
            duplicateChecker.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
        }
    }
}
