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

    @JsonProperty("court_type")
    private String courtType;

    @JsonProperty("court_type_id")
    private String courtTypeId;

    @JsonProperty("cluster_id")
    private String clusterId;

    @JsonProperty("cluster_name")
    private String clusterName;

    @JsonProperty("open_for_public")
    private String openForPublic;

    @JsonProperty("court_address")
    private String courtAddress;

    @JsonProperty("postcode")
    private String postCode;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("closed_date")
    private String closedDate;

    @JsonProperty("court_location_code")
    private String courtLocationCode;

    @JsonProperty("dx_address")
    private String dxAddress;

    @JsonProperty("welsh_site_name")
    private String welshSiteName;

    @JsonProperty("welsh_court_address")
    private String welshCourtAddress;

    @JsonProperty("court_status")
    private String courtStatus;

    @JsonProperty("court_open_date")
    private String courtOpenDate;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("is_case_management_location")
    private String isCaseManagementLocation;

    @JsonProperty("is_hearing_location")
    private String isHearingLocation;

    @JsonProperty("welsh_venue_name")
    private String welshVenueName;

    @JsonProperty("is_temporary_location")
    private String isTemporaryLocation;

    @JsonProperty("is_nightingale_court")
    private String isNightingaleCourt;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("parent_location")
    private String parentLocation;

    @JsonProperty("welsh_court_name")
    private String welshCourtName;

    @JsonProperty("uprn")
    private String uprn;

    @JsonProperty("venue_ou_code")
    private String venueOuCode;

    @JsonProperty("mrd_building_location_id")
    private String mrdBuildingLocationId;

    @JsonProperty("mrd_venue_id")
    private String mrdVenueId;

    @JsonProperty("service_url")
    private String serviceUrl;

    @JsonProperty("fact_url")
    private String factUrl;

}
