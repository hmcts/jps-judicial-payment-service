package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import uk.gov.hmcts.reform.jps.config.Endpoints;
import uk.gov.hmcts.reform.jps.config.TestVariables;
import uk.gov.hmcts.reform.jps.testutils.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.testutils.ServiceAuthenticationGenerator;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

public class StepDefinitions extends TestVariables {

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
        } else if (role.equalsIgnoreCase("ccd-admin")) {
            accessToken = idamTokenGenerator.authenticateUser(adminUsername, adminPassword);
        } else if (role.equalsIgnoreCase("ccd-publisher")) {
            accessToken = idamTokenGenerator.authenticateUser(publisherUsername, publisherPassword);
        } else if (role.equalsIgnoreCase("ccd-submitter")) {
            accessToken = idamTokenGenerator.authenticateUser(submitterUsername, submitterPassword);
        } else if (role.equalsIgnoreCase("ccd-import")) {
            accessToken = idamTokenGenerator.authenticateUser(invalidUsername, invalidPassword);
        }
    }

    @Given("a sitting record is created")
    public void sittingRecordIsCreated() {
        //to be added once post sitting records is ready
    }

    @Given("a sitting record is created by a different user")
    public void sittingRecordIsCreatedByDifferentUser() {
        //to be added once post sitting records is ready
    }

    @Given("a sitting record is created and its status is not {string}")
    public void sittingRecordIsCreatedAndItsStatusIsNot(String string) {
        //to be added once post sitting records is ready
    }

    @When("the request is prepared with appropriate values")
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

    @When("the request is missing the S2S token")
    public void requestIsMissingTheS2sToken() {
        given = given.header("ServiceAuthorization", null);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void callIsSubmittedToTheEndpoint(String resource, String method) {
        Endpoints endpoint = Endpoints.valueOf(resource);

        if (method.equalsIgnoreCase("POST")) {
            response = given.when().post(endpoint.getEndpoint());
        } else if (method.equalsIgnoreCase("GET")) {
            response = given.when().get(endpoint.getEndpoint());
        } else if (method.equalsIgnoreCase("DELETE")) {
            response = given.when().delete(endpoint.getEndpoint());
        }
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
