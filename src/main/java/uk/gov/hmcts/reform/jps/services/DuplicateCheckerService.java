package uk.gov.hmcts.reform.jps.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.EvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateMatchingDuration;
import uk.gov.hmcts.reform.jps.components.EvaluateOverlapDuration;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;

@Service
public class DuplicateCheckerService {
    private final EvaluateDuplicate evaluateDuplicate;
    private final EvaluateMatchingDuration evaluateMatchingDuration;
    private final EvaluateOverlapDuration evaluateOverlapDuration;

    @Autowired
    public DuplicateCheckerService(EvaluateDuplicate evaluateDuplicate,
                                EvaluateMatchingDuration evaluateMatchingDuration,
                                EvaluateOverlapDuration evaluateOverlapDuration) {
        this.evaluateDuplicate = evaluateDuplicate;
        this.evaluateMatchingDuration = evaluateMatchingDuration;
        this.evaluateOverlapDuration = evaluateOverlapDuration;

        this.evaluateDuplicate.next(this.evaluateMatchingDuration);
        this.evaluateMatchingDuration.next(this.evaluateOverlapDuration);
    }

    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields fields) {
        evaluateDuplicate.evaluate(sittingRecordWrapper, fields);
    }
}
