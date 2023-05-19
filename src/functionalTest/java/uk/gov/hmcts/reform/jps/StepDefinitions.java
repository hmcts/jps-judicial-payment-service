package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.reform.jps.config.PropertiesReader;
import uk.gov.hmcts.reform.jps.testutils.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.testutils.ServiceAuthenticationGenerator;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@SuppressWarnings("CheckStyle")
public class StepDefinitions {

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String testUrl = propertiesReader.getProperty("test-url");
    String recorderUsername = propertiesReader.getProperty("idam.recorder.username");
    String recorderPassword = propertiesReader.getProperty("idam.recorder.password");


    RequestSpecification request;
    RequestSpecification given;
    Response response;
    String accessToken;

    @Given("a user with the IDAM role of {string}")
    public void a_user_with_the_idam_role_of(String string) {

        IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();
        accessToken = idamTokenGenerator.authenticateUser(recorderUsername, recorderPassword);

        System.out.println("newwwwwwwwwww " + accessToken);
    }

    @When("a request is prepared with appropriate values")
    public void a_request_is_prepared_with_appropriate_values() {

        ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();
        String s2sToken = serviceAuthenticationGenerator.generate();

        System.out.println("heree " + s2sToken);

        request = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setContentType(ContentType.JSON)
            .addHeader("ServiceAuthorization", s2sToken)
            .addHeader("Authorization", accessToken)
            .build();


        given = given().log().all().spec(request);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void a_call_is_submitted_to_the_endpoint_using_a_request(String resource, String method) {

        response = given.when().get("/test");
    }

    @Then("a {string} response is received with a {string} status code")
    public void a_response_is_received_with_a_status_code(String responseType, String responseCode) {

        response.then().log().all().extract().response().asString();
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
