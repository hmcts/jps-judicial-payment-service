package uk.gov.hmcts.reform.jps.services.refdata;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.exceptions.UnknownValueException;
import uk.gov.hmcts.reform.jps.refdata.location.client.LocationApi;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceClientTest {
    @Mock
    private IdamTokenGenerator idamTokenGenerator;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private LocationApi locationApi;
    @Mock
    private FeignException feignException;
    @InjectMocks
    private LocationServiceClient locationServiceClient;

    @Test
    void shouldReturnLocationApiResponseWhenValidCourtVenueIsLookedUp() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(locationApi.getCourtDetailsByServiceCode("idamToken", "serviceToken", "123"))
            .thenReturn(LocationApiResponse.builder().build());

        LocationApiResponse locationApiResponse = locationServiceClient.getCourtVenue("123");
        assertThat(locationApiResponse).isNotNull();
    }

    @Test
    void shouldThrowUnknownValueExceptionWhenInValidCourtVenueIsLookedUp() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(feignException.status()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(locationApi.getCourtDetailsByServiceCode("idamToken", "serviceToken", "123"))
            .thenThrow(feignException);

        assertThatThrownBy(() -> locationServiceClient.getCourtVenue("123"))
            .isInstanceOf(UnknownValueException.class)
            .hasMessage("004 unknown hmctsServiceCode");
    }

    @Test
    void shouldThrowFeignExceptionWhenCourtVenueCallFails() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(locationApi.getCourtDetailsByServiceCode("idamToken", "serviceToken", "123"))
            .thenThrow(feignException);

        assertThatThrownBy(() -> locationServiceClient.getCourtVenue("123"))
            .isInstanceOf(FeignException.class);
    }
}
