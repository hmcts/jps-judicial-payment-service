package uk.gov.hmcts.reform.jps;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.wiremock.extensions.DynamicOAuthJwkSetResponseTransformer;

import javax.inject.Inject;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    Application.class,
    TestIdamConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
public class BaseTest {

    @Inject
    protected SecurityUtils securityUtils;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @Mock
    protected Authentication authentication;

    public static final String RESET_DATABASE
        = "classpath:sql/reset_database.sql";
    public static final String ADD_SITTING_RECORD_STATUS_HISTORY
        = "classpath:sql/add_sitting_record_status_history.sql";

    public static final String ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY
        = "classpath:sql/add_submit_sitting_record_status_history.sql";

    public static final String SITTING_RECORD_STATUS_HISTORY
        = "classpath:sql/sitting_record_status_history.sql";
    public static final String INSERT_COURT_VENUE
        = "classpath:sql/insert_court_venue.sql";
    public static final String INSERT_FEE
        = "classpath:sql/insert_fee.sql";

    public static final String INSERT_SERVICE
        = "classpath:sql/insert_service_test_data.sql";

    public static final String INSERT_JOH
        = "classpath:sql/insert_joh.sql";

    public static final String INSERT_SERVICE_TEST_DATA
        = "classpath:sql/insert_service_test_data.sql";

    public static final String INSERT_PUBLISHED_TEST_DATA
        = "classpath:sql/insert_published_test_data.sql";

    public static final String INSERT_EVALUATE_PUBLISHED_TEST_DATA
        = "classpath:sql/insert_evaluate_published_test_data.sql";

    public static final String INSERT_SUBMITTED_TEST_DATA
        = "classpath:sql/insert_submitted_test_data.sql";

    @BeforeEach
    void init() {
        final String hostUrl = "http://localhost:" + wiremockPort;

        Jwt jwt = dummyJwt();
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        final String jsonText = """
        {
            "jurisdiction": "Jurisdiction1",
            "case_type": "CaseType1"
        }
            """;

        stubFor(WireMock.get(urlMatching("/cases/.*"))
                    .willReturn(okJson(jsonText)));
    }

    @Configuration
    static class WireMockTestConfiguration {
        @Bean
        public WireMockConfigurationCustomizer wireMockConfigurationCustomizer() {
            return config -> config.extensions(
                new WiremockFixtures.ConnectionClosedTransformer(),
                new DynamicOAuthJwkSetResponseTransformer()
            );
        }
    }

    private Jwt dummyJwt() {
        return Jwt.withTokenValue("a dummy jwt token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
    }
}

