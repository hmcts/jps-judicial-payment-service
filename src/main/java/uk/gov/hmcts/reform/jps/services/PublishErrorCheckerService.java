package uk.gov.hmcts.reform.jps.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.CourtVenueErrorChecker;
import uk.gov.hmcts.reform.jps.components.FeeInErrorChecker;
import uk.gov.hmcts.reform.jps.components.JohAttributesErrorChecker;
import uk.gov.hmcts.reform.jps.components.JohPayrollErrorChecker;
import uk.gov.hmcts.reform.jps.components.ServiceErrorChecker;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;

@Service
public class PublishErrorCheckerService {
    private final CourtVenueErrorChecker courtVenueErrorChecker;

    private final JohAttributesErrorChecker johAttributesErrorChecker;
    private final JohPayrollErrorChecker johPayrollErrorChecker;
    private final FeeInErrorChecker feeInErrorChecker;

    @Autowired
    public PublishErrorCheckerService(CourtVenueErrorChecker courtVenueErrorChecker,
                                      JohAttributesErrorChecker johAttributesErrorChecker,
                                      JohPayrollErrorChecker johPayrollErrorChecker,
                                      FeeInErrorChecker feeInErrorChecker,
                                      ServiceErrorChecker serviceErrorChecker) {
        this.courtVenueErrorChecker = courtVenueErrorChecker;
        this.johAttributesErrorChecker = johAttributesErrorChecker;
        this.johPayrollErrorChecker = johPayrollErrorChecker;
        this.feeInErrorChecker = feeInErrorChecker;

        this.courtVenueErrorChecker.next(johAttributesErrorChecker);
        this.johAttributesErrorChecker.next(johPayrollErrorChecker);
        this.johPayrollErrorChecker.next(feeInErrorChecker);
        this.feeInErrorChecker.next(serviceErrorChecker);
    }

    public void evaluate(String hmctsServiceCode,
                                  SittingRecordPublishProjection.SittingRecordPublishFields sittingRecord,
                                  PublishErrors publishErrors) {
        courtVenueErrorChecker.evaluate(publishErrors,
                                        hmctsServiceCode,
                                        sittingRecord);
    }
}
