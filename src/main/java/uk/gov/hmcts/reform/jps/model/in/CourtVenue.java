package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;

@Data
public class CourtVenue {
    private String epimmsId;

    private String hmctsServiceId;

    private String costCenterCode;
}
