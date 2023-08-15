package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.caseworker.client.CaseWorkerApi;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CaseWorkerClient {
    private final IdamTokenGenerator idamTokenGenerator;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseWorkerApi caseWorkerApi;

    public CaseWorkerApiResponse getCaseWorkerDetails(String id) {

        return caseWorkerApi.getCaseWorkerDetails(
            idamTokenGenerator.generateIdamTokenForRefData(),
            authTokenGenerator.generate(),
            id
        );
    }
}
