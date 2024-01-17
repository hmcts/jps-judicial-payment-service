package uk.gov.hmcts.reform.jps.components;

import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;

public interface PublishErrorChecker {
    void next(PublishErrorChecker publishErrorChecker);

    void evaluate(PublishErrors publishErrors, String hmctsServiceCode,
                  SittingRecordPublishProjection.SittingRecordPublishFields sittingRecord);
}
