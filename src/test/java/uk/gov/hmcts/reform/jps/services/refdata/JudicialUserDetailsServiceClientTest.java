package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.judicial.client.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudicialUserDetailsServiceClientTest {

    @Mock
    private IdamTokenGenerator idamTokenGenerator;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private JudicialUserDetailsApi judicialUserDetailsApi;
    @InjectMocks
    private JudicialUserDetailsServiceClient client;

    @Test
    void shouldReturnCaseWorkerApiResponseWhenValidCaseWorkerIsLookedUp() {
        JudicialUserDetailsApiRequest request = JudicialUserDetailsApiRequest.builder().build();
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(judicialUserDetailsApi.getJudicialUserDetails("idamToken", "serviceToken", request))
            .thenReturn(List.of(JudicialUserDetailsApiResponse.builder().build()));

        List<JudicialUserDetailsApiResponse> judicialUserDetails = client.getJudicialUserDetails(
            request);
        assertThat(judicialUserDetails).isNotNull();
    }
}
