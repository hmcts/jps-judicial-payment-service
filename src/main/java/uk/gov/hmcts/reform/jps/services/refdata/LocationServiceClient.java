package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.location.client.LocationApi;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class LocationServiceClient {
    private final IdamTokenGenerator idamTokenGenerator;
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationApi locationApi;

    public LocationApiResponse getCourtVenue(String hmctsServiceCode) {
        return locationApi.getCourtDetailsByServiceCode(
            idamTokenGenerator.generateIdamTokenForRefData(),
            authTokenGenerator.generate(),
            hmctsServiceCode
        );
    }
}
