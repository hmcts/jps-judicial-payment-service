package uk.gov.hmcts.reform.jps.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class IdamTokenGenerator {

    @Value("${idam.jps.system.username}")
    private String refDataUserName;

    @Value("${idam.jps.system.password}")
    private String refDataPassword;

    private final IdamClient idamClient;

    public String generateIdamTokenForRefData() {
        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }
}
