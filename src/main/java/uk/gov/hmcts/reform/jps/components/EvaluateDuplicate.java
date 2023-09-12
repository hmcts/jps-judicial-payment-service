package uk.gov.hmcts.reform.jps.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;

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
        sittingRecordWrapper.setSittingRecordId(sittingRecordDuplicateCheckFields.getId());
        duplicateChecker.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }
}
