package uk.gov.hmcts.reform.jps.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.EvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateMatchingDuration;
import uk.gov.hmcts.reform.jps.components.EvaluateOverlapDuration;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;

@Service
public class DuplicateCheckerService {
    private final EvaluateDuplicate evaluateDuplicate;
    private final EvaluateMatchingDuration evaluateMatchingDuration;
    private final EvaluateOverlapDuration evaluateOverlapDuration;
    private final StatusHistoryService statusHistoryService;

    @Autowired
    public DuplicateCheckerService(EvaluateDuplicate evaluateDuplicate,
                                   EvaluateMatchingDuration evaluateMatchingDuration,
                                   EvaluateOverlapDuration evaluateOverlapDuration,
                                   StatusHistoryService statusHistoryService) {
        this.evaluateDuplicate = evaluateDuplicate;
        this.evaluateMatchingDuration = evaluateMatchingDuration;
        this.evaluateOverlapDuration = evaluateOverlapDuration;
        this.statusHistoryService = statusHistoryService;

        this.evaluateDuplicate.next(this.evaluateMatchingDuration);
        this.evaluateMatchingDuration.next(this.evaluateOverlapDuration);
    }

    public void evaluate(SittingRecordWrapper sittingRecordWrapper,
                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields fields) {
        evaluateDuplicate.evaluate(sittingRecordWrapper, fields);

        if (sittingRecordWrapper.getErrorCode() != VALID
            && !TRUE.equals(sittingRecordWrapper.getSittingRecordRequest().getReplaceDuplicate())) {
            statusHistoryService.updateFromStatusHistory(
                sittingRecordWrapper,
                fields
            );
            sittingRecordWrapper.setJudgeRoleTypeId(fields.getJudgeRoleTypeId());
            sittingRecordWrapper.setAm(fields.getAm());
            sittingRecordWrapper.setPm(fields.getPm());
        }
    }

}
