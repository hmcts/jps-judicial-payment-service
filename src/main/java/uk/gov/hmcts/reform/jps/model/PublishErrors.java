package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class PublishErrors {
    @Setter
    private boolean error;
    @Setter
    private int recordsInError;
    private final List<String> serviceInErrors = new ArrayList<>();
    private final List<CourtVenueInError> courtVenueInErrors = new ArrayList<>();
    private final List<FeeInError> feeInErrors = new ArrayList<>();
    private final List<JohAttributesInError> johAttributesInErrors = new ArrayList<>();
    private final List<JohPayrollInError> johPayrollInErrors = new ArrayList<>();


    public void addServiceError(String hmctsServiceId) {
        serviceInErrors.add(hmctsServiceId);
    }

    public void addCourtVenueError(CourtVenueInError courtVenueInError) {
        courtVenueInErrors.add(courtVenueInError);
    }


    public void addFeeError(FeeInError feeInError) {
        feeInErrors.add(feeInError);
    }

    public void addJohAttributesError(JohAttributesInError johAttributesInError) {
        johAttributesInErrors.add(johAttributesInError);
    }

    public void addJohPayrollErrors(JohPayrollInError johPayrollInError) {
        johPayrollInErrors.add(johPayrollInError);
    }

    public int getErrorCount() {
        return serviceInErrors.size()
            + courtVenueInErrors.size()
            + feeInErrors.size()
            + johAttributesInErrors.size()
            + johPayrollInErrors.size();
    }
}
