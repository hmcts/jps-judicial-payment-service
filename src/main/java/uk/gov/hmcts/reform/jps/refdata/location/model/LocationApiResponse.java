package uk.gov.hmcts.reform.jps.refdata.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationApiResponse {
    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("court_venues")
    private List<CourtVenue> courtVenues;
}
