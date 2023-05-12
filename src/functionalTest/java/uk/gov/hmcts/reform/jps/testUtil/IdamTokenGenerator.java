package uk.gov.hmcts.reform.jps.testUtil;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.concurrent.TimeUnit;

@TestPropertySource("classpath:application.yaml")
@NoArgsConstructor
public final class IdamTokenGenerator {

    @Value("${idam.recorder.username}")
    private String recorderUsername;

    @Value("${idam.recorder.password}")
    private String recorderPassword;

    @Value("${idam.submitter.username}")
    private String submitterUsername;

    @Value("${idam.submitter.password}")
    private String submitterPassword;

    @Value("${idam.publisher.username}")
    private String publisherUsername;

    @Value("${idam.publisher.password}")
    private String publisherPassword;

    @Value("${idam.admin.username}")
    private String adminUsername;

    @Value("${idam.admin.password}")
    private String adminPassword;

    @Autowired
    private IdamClient idamClient;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    public String generateIdamTokenForRecorder() {
        String recorderUserToken = cache.getIfPresent(recorderUsername);
        if (recorderUserToken == null) {
            recorderUserToken = idamClient.getAccessToken(recorderUsername, recorderPassword);
            cache.put(recorderUsername, recorderUserToken);
        }
        return recorderUserToken;
    }

    public String generateIdamTokenForSubmitter() {
        return idamClient.getAccessToken(submitterUsername, submitterPassword);
    }

    public String generateIdamTokenForPublisher() {
        return idamClient.getAccessToken(publisherUsername, publisherPassword);
    }

    public String generateIdamTokenForAdmin() {
        return idamClient.getAccessToken(adminUsername, adminPassword);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
