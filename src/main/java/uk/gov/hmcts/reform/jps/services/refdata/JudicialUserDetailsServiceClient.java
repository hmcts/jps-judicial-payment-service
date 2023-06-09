package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.judicial.client.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.util.List;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialUserDetailsServiceClient {
    private final IdamTokenGenerator idamTokenGenerator;
    private final AuthTokenGenerator authTokenGenerator;
    private final JudicialUserDetailsApi judicialUserDetailsApi;

    public List<JudicialUserDetailsApiResponse> getJudicialUserDetails(
        JudicialUserDetailsApiRequest judicialUsersApiRequest) {

        return judicialUserDetailsApi.getJudicialUserDetails(
            idamTokenGenerator.generateIdamTokenForRefData(),
            authTokenGenerator.generate(),
            judicialUsersApiRequest
        );
    }
}
