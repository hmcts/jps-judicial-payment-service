package uk.gov.hmcts.reform.jps.testutils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import uk.gov.hmcts.reform.jps.config.TestVariables;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class ServiceAuthenticationGenerator  extends TestVariables {

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String s2sUrl = propertiesReader.getProperty("idam.s2s-auth.url");
    String s2sName = propertiesReader.getProperty("s2s.name");

    public String generate() {
        return generate(this.s2sName);
    }

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

        return response.getBody().asString();
    }
}
