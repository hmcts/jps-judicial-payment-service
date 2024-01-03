package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ServiceErrorChecker extends ErrorChecker {
    private final ServiceService serviceService;

    @Override
    public void evaluate(PublishErrors publishErrors,
                         String hmctsServiceCode,
                         SittingRecordPublishProjection.SittingRecordPublishFields sittingRecord) {
        evaluate(publishErrors, hmctsServiceCode, sittingRecord,
                 () -> serviceService.isServiceOnboarded(hmctsServiceCode)
                 ? Optional.of(Boolean.TRUE) : Optional.empty()
        );
    }

    @Override
    protected void addErrorInfo(PublishErrors publishErrors,
                                String hmctsServiceCode,
                                SittingRecordPublishProjection.SittingRecordPublishFields sittingRecord) {
        publishErrors.addServiceError(hmctsServiceCode);
    }
}
