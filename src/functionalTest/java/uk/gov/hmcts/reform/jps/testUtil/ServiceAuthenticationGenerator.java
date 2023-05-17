package uk.gov.hmcts.reform.jps.testUtil;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import uk.gov.hmcts.reform.jps.config.PropertiesReader;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//@TestPropertySource("classpath:application.yaml")
//@Service
public class ServiceAuthenticationGenerator {

//    private TestConfigProperties testProps;


//    @Value("${s2s.name}")
//    private String s2sName;
//
//    @Value("${idam.s2s-auth.url}")
//    private String s2sUrl;

//    @Value("${s2s.name}")
//    private String s2sName = "jps_webapp";
//
//    @Value("${idam.url}")
//    private String s2sUrl = "http://localhost:8489";
//
//    public String values(String s2sName) {
//        this.testProps = testProps;
//        return values(testProps.getS2sName());
//    }

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String s2sUrl = propertiesReader.getProperty("idam.s2s-auth.url");
    String s2sName = propertiesReader.getProperty("s2s.name");

    public String generate() {
        return generate(this.s2sName);
    }


//    @Autowired
//    private Environment environment;
//
//    String s2sName = environment.getProperty("s2s.name");
//    String s2sUrl = environment.getProperty("idam.url");


    public String generate(final String s2sName) {
        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", s2sName))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().asString();
    }
}
