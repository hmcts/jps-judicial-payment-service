package uk.gov.hmcts.reform.jps.components;

import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;

public interface DuplicateChecker {
    void next(DuplicateChecker duplicateChecker);

    void evaluate(SittingRecordWrapper sittingRecordWrapper,
                     SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                         sittingRecordDuplicateCheckFields);
}
