package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

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
                    .siteName("one")
                    .build(),
                CourtVenue.builder()
                    .regionId("2")
                    .siteName("two")
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

        locationService.setRegionDetails("serviceCode",
                                         sittingRecords);

        assertThat(sittingRecords)
            .extracting("regionId", "regionName")
            .contains(
                tuple("2", "two"),
                tuple("1", "one"),
                tuple("3", "N/A")
            );
    }
}
