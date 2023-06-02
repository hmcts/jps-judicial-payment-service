package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.caseworker.client.CaseWorkerApi;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerClientTest {

    @Mock
    private IdamTokenGenerator idamTokenGenerator;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseWorkerApi caseWorkerApi;
    @InjectMocks
    private CaseWorkerClient caseWorkerClient;

    @Test
    void shouldReturnCaseWorkerApiResponseWhenValidCaseWorkerIsLookedUp() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("idamToken");
        when(authTokenGenerator.generate()).thenReturn("serviceToken");
        when(caseWorkerApi.getCaseWorkerDetails("idamToken", "serviceToken", "123"))
            .thenReturn(CaseWorkerApiResponse.builder().build());

        CaseWorkerApiResponse caseWorkerDetails = caseWorkerClient.getCaseWorkerDetails("123");
        assertThat(caseWorkerDetails).isNotNull();
    }
}
