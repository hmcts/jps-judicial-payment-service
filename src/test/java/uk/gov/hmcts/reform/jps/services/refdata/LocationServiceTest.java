package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
}
