package uk.gov.hmcts.reform.jps.refdata.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtVenue {
    @JsonProperty("court_venue_id")
    private String courtVenueId;

    @JsonProperty("epimms_id")
    private String epimmsId;

    @JsonProperty("site_name")
    private String siteName;

    @JsonProperty("region_id")
    private String regionId;

    @JsonProperty("region")
    private String region;
}
