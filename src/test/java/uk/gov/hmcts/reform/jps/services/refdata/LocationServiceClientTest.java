package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.location.client.LocationApi;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceClientTest {
    @Mock
    private IdamTokenGenerator idamTokenGenerator;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private LocationApi locationApi;
    @InjectMocks
    private LocationServiceClient locationServiceClient;

    @Test
    void shouldReturnCaseWorkerApiResponseWhenValidCaseWorkerIsLookedUp() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(locationApi.getCourtDetailsByServiceCode("idamToken", "serviceToken", "123"))
            .thenReturn(LocationApiResponse.builder().build());

        LocationApiResponse locationApiResponse = locationServiceClient.getCourtVenue("123");
        assertThat(locationApiResponse).isNotNull();
    }
}
