package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtVenueInError {
    private String hmctsServiceId;
    private String epimmsId;
}
