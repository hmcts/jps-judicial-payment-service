package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_LOCATION;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationServiceClient locationServiceClient;

    @InjectMocks
    private LocationService locationService;

    @Test
    void shouldSetRegionNameWhenRegionDetailsFound() {
        LocationApiResponse locationApiResponse = LocationApiResponse.builder()
            .serviceCode("serviceCode")
            .courtVenues(List.of(
                CourtVenue.builder()
                    .regionId("1")
                    .siteName("one details")
                    .region("one")
                    .build(),
                CourtVenue.builder()
                    .regionId("2")
                    .siteName("two details")
                    .region("two")
                    .build()
            ))
            .build();
        List<SittingRecord> sittingRecords = List.of(
            SittingRecord.builder()
                .regionId("2")
                .build(),
            SittingRecord.builder()
                .regionId("1")
                .build(),
            SittingRecord.builder()
                .regionId("3")
                .build()
        );
        when(locationServiceClient.getCourtVenue("serviceCode"))
            .thenReturn(locationApiResponse);

        locationService.setRegionName("serviceCode",
                                      sittingRecords);

        assertThat(sittingRecords)
            .extracting("regionId", "regionName")
            .contains(
                tuple("2", "two"),
                tuple("1", "one"),
                tuple("3", "N/A")
            );
    }

    @Test
    void shouldSetRegionIdWhenRegionDetailsFound() {
        LocationApiResponse locationApiResponse = LocationApiResponse.builder()
            .serviceCode("serviceCode")
            .courtVenues(List.of(
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("one")
                    .build(),
                CourtVenue.builder()
                    .epimmsId("2")
                    .regionId("2")
                    .siteName("two")
                    .build()
            ))
            .build();
        List<SittingRecordRequest> sittingRecordRequest = List.of(
            SittingRecordRequest.builder()
                .epimmsId("2")
                .build(),
            SittingRecordRequest.builder()
                .epimmsId("1")
                .build()
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            sittingRecordRequest.stream()
                .map(SittingRecordWrapper::new)
                .toList();

        when(locationServiceClient.getCourtVenue("serviceCode"))
            .thenReturn(locationApiResponse);

        locationService.setRegionId("serviceCode",
                                    sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .map(SittingRecordWrapper::getRegionId)
            .contains("2", "1");
    }


    @Test
    void shouldSetInvalidLocationWhenRegionDetailsNotFound() {
        LocationApiResponse locationApiResponse = LocationApiResponse.builder()
            .serviceCode("serviceCode")
            .courtVenues(List.of(
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("one")
                    .build()
            ))
            .build();
        List<SittingRecordRequest> sittingRecordRequest = List.of(
            SittingRecordRequest.builder()
                .epimmsId("3")
                .build()
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            sittingRecordRequest.stream()
                .map(SittingRecordWrapper::new)
                .toList();

        when(locationServiceClient.getCourtVenue("serviceCode"))
            .thenReturn(locationApiResponse);

        locationService.setRegionId("serviceCode",
                                    sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode")
            .contains(INVALID_LOCATION);
    }

    @Test
    void shouldReturnCourtVenuesWhenServiceCodeIsValid() {
        LocationApiResponse locationApiResponse = LocationApiResponse.builder()
            .serviceCode("serviceCode")
            .courtVenues(List.of(
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("one")
                    .build(),
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("two")
                    .build(),
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("three")
                    .build()
            ))
            .build();

        when(locationServiceClient.getCourtVenue("serviceCode"))
            .thenReturn(locationApiResponse);

        List<CourtVenue> courtVenues = locationService.getCourtVenues("serviceCode");
        assertThat(courtVenues)
            .hasSize(3);
    }

    @Test
    void testGetVenueName() {
        ArrayList<CourtVenue> courtVenues = new ArrayList<>();
        CourtVenue courtVenue = createCourtVenue();
        courtVenues.add(courtVenue);
        LocationApiResponse buildResult = LocationApiResponse.builder()
            .courtVenues(courtVenues)
            .serviceCode("Service Code")
            .build();
        when(locationServiceClient.getCourtVenue(Mockito.<String>any())).thenReturn(buildResult);
        assertEquals("Venue Name", locationService.getVenueName("Hmcts Service Code", "EP42"));
        verify(locationServiceClient).getCourtVenue(Mockito.<String>any());
    }

    @Test
    void testGetCourtName() {
        ArrayList<CourtVenue> courtVenues = new ArrayList<>();
        CourtVenue courtVenue = createCourtVenue();
        courtVenues.add(courtVenue);
        LocationApiResponse buildResult = LocationApiResponse.builder()
            .courtVenues(courtVenues)
            .serviceCode("Service Code")
            .build();
        when(locationServiceClient.getCourtVenue(Mockito.<String>any())).thenReturn(buildResult);
        assertEquals("Court Name", locationService.getCourtName("Hmcts Service Code", "EP42"));
        verify(locationServiceClient).getCourtVenue(Mockito.<String>any());
    }

    private CourtVenue createCourtVenue() {
        return CourtVenue.builder()
            .closedDate("2020-03-01")
            .clusterId("CI42")
            .clusterName("Cluster Name")
            .courtAddress("42 Main St")
            .courtLocationCode("Court Location Code")
            .courtName("Court Name")
            .courtOpenDate("2020-03-01")
            .courtStatus("Court Status")
            .courtType("Court Type")
            .courtTypeId("CTI42")
            .courtVenueId("CVI42")
            .dxAddress("42 Main St")
            .epimmsId("EP42")
            .factUrl("https://example.org/example")
            .locationType("Location Type")
            .mrdBuildingLocationId("MBLI42")
            .mrdVenueId("MVI42")
            .openForPublic("Open For Public")
            .parentLocation("Parent Location")
            .phoneNumber("6625550144")
            .postCode("OX1 1PT")
            .region("us-east-2")
            .regionId("us-east-2")
            .serviceUrl("https://example.org/example")
            .siteName("Site Name")
            .uprn("Uprn")
            .venueName("Venue Name")
            .venueOuCode("Venue Ou Code")
            .welshCourtAddress("42 Main St")
            .welshCourtName("Welsh Court Name")
            .welshSiteName("Welsh Site Name")
            .welshVenueName("Welsh Venue Name")
            .build();
    }
}
