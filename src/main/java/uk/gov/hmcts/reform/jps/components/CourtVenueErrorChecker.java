package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.CourtVenueInError;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.services.CourtVenueService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtVenueErrorChecker extends ErrorChecker {
    private final CourtVenueService courtVenueService;

    @Override
    public void evaluate(PublishErrors publishErrors,
                         String hmctsServiceCode,
                         SittingRecordPublishFields sittingRecord) {
        LOGGER.debug("evaluate");
        evaluate(publishErrors, hmctsServiceCode, sittingRecord,
                 () ->
                     courtVenueService.getCourtVenue(
                         hmctsServiceCode,
                         sittingRecord.getEpimmsId()
                     )
        );
    }

    @Override
    protected void addErrorInfo(PublishErrors publishErrors,
                                String hmctsServiceCode,
                                SittingRecordPublishFields sittingRecord) {
        publishErrors.addCourtVenueError(CourtVenueInError.builder()
                                             .hmctsServiceId(hmctsServiceCode)
                                             .epimmsId(sittingRecord.getEpimmsId())
                                             .build());
    }
}
