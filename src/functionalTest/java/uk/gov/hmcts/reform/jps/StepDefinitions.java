package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import uk.gov.hmcts.reform.hmc.jp.functional.resources.APIResources;
import uk.gov.hmcts.reform.jps.config.PropertiesReader;
import uk.gov.hmcts.reform.jps.testutils.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.testutils.ServiceAuthenticationGenerator;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

public class StepDefinitions {

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String testUrl = propertiesReader.getProperty("test-url");
    String recorderUsername = propertiesReader.getProperty("idam.recorder.username");
    String recorderPassword = propertiesReader.getProperty("idam.recorder.password");
    String invalidUsername = propertiesReader.getProperty("idam.invalid.username");
    String invalidPassword = propertiesReader.getProperty("idam.invalid.password");

    RequestSpecification request;
    RequestSpecification given;
    Response response;
    String accessToken;

    ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();

    @Given("a user with the IDAM role of {string}")
    public void userWithTheIdamRoleOf(String role) {

        IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();

        if (role.equalsIgnoreCase("jps-recorder")) {
            accessToken = idamTokenGenerator.authenticateUser(recorderUsername, recorderPassword);
        } else if (role.equalsIgnoreCase("ccd-import")) {
            accessToken = idamTokenGenerator.authenticateUser(invalidUsername, invalidPassword);
        }
    }

    @Given("a sitting record is created")
    public void a_sitting_record_is_created() {

    }

    @Given("a sitting record is created by a different user")
    public void a_sitting_record_is_created_by_a_different_user() {

    }

    @Given("a sitting record is created ans its status is not {string}")
    public void a_sitting_record_is_created_ans_its_status_is_not(String string) {

    }

    @When("a request is prepared with appropriate values")
    public void requestIsPreparedWithAppropriateValues() {

        request = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setContentType(ContentType.JSON)
            .addHeader("Authorization", accessToken)
            .build();

        given = given().log().all().spec(request);
    }

    @When("the request contains a valid service token")
    public void theRequestContainsValidServiceToken() {

        String s2sToken = serviceAuthenticationGenerator.generate();
        given = given.header("ServiceAuthorization", s2sToken);
    }

    @When("the request contains an invalid service token")
    public void theRequestContainsInvalidServiceToken() {

        String s2sToken = serviceAuthenticationGenerator.generate("xui_webapp");
        given = given.header("ServiceAuthorization", s2sToken);
    }

    @When("a request is missing the S2S token")
    public void a_request_is_missing_the_s2s_token() {
        request = new RequestSpecBuilder().setBaseUri("http://localhost:3000").setContentType(
            ContentType.JSON).build();
        given = given().log().all().spec(request);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void callIsSubmittedToTheEndpoint(String resource, String method) {

        response = given.when().get("/test");
        APIResources resourceAPI = APIResources.valueOf(resource);

        if (method.equalsIgnoreCase("POST"))
            response = given.when().post(resourceAPI.getResource());
        else if (method.equalsIgnoreCase("GET"))
            response = given.when().get(resourceAPI.getResource());
        else if (method.equalsIgnoreCase("DELETE"))
            response = given.when().delete(resourceAPI.getResource());
    }

    @Then("a {string} response is received with a {string} status code")
    public void responseIsReceivedWithStatusCode(String responseType, String responseCode) {

        response.then().log().all().extract().response().asString();

        if (responseType.equalsIgnoreCase("positive")) {
            if (responseCode.equalsIgnoreCase("200 OK")) {
                assertThat(response.getStatusCode()).isEqualTo(OK.value());
            }
        } else {
            if (responseCode.equalsIgnoreCase("400 Bad Request")) {
                assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
            } else if (responseCode.equalsIgnoreCase("401 Unauthorised")) {
                assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
            } else if (responseCode.equalsIgnoreCase("403 Forbidden")) {
                assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
            }
        }
    }

    @When("the request contains the {string} as {string}")
    public void theRequestContainsTheAs(String pathParam, String value) {
        given = given().log().all().pathParam(pathParam,value).spec(request);
    }

    @Then("the response is empty")
    public void theResponseIsEmpty() {
        response.then().assertThat().body("isEmpty()", Matchers.is(true));
    }
}
