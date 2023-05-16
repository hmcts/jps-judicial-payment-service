package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.jps.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.refdata.location.client.LocationApi;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class LocationServiceClient {
    private final IdamTokenGenerator idamTokenGenerator;
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationApi locationApi;

    public LocationApiResponse getCourtVenue(String hmctsServiceCode) {
        log.info("idamTokenGenerator {}", idamTokenGenerator.generateIdamTokenForRefData());
        log.info("authTokenGenerator {}", authTokenGenerator.generate());
        return locationApi.getCourtDetailsByServiceCode(
            idamTokenGenerator.generateIdamTokenForRefData(),
            authTokenGenerator.generate(),
            hmctsServiceCode
        );
    }
}
