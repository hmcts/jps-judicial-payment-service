package uk.gov.hmcts.reform.jps;

import io.cucumber.java.Before;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class StepDefinitions extends TestVariables {

    RequestSpecification request;
    RequestSpecification given;
    Response response;

    private static String accessToken;
    private static String recorderAccessToken;
    private static String invalidAccessToken;
    private static String validS2sToken;
    private static String invalidS2sToken;
    private static boolean isSetupExecuted = false;

    @Before
    public void setup() throws InterruptedException {
        if (!isSetupExecuted) {
            IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();
            ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();

            recorderAccessToken = idamTokenGenerator.authenticateUser(recorderUsername, recorderPassword);
            invalidAccessToken = idamTokenGenerator.authenticateUser(invalidUsername, invalidPassword);
            validS2sToken = serviceAuthenticationGenerator.generate();
            invalidS2sToken = serviceAuthenticationGenerator.generate("xui_webapp");

            isSetupExecuted = true;
        }
    }

    @Given("a user with the IDAM role of {string}")
    public void userWithTheIdamRoleOf(String role) {
        if (role.equalsIgnoreCase("jps-recorder")) {
            accessToken  = recorderAccessToken;
        } else if (role.equalsIgnoreCase("ccd-import")) {
            accessToken  = invalidAccessToken;
        }
    }

    @Given("a record for the given hmctsServiceCode exists in the database")
    public void recordForTheGivenHmctsServiceCodeExistsInTheDatabase() {
        // POST call needs to be added here
    }

    @When("a request is prepared with appropriate values")
    public void requestIsPreparedWithAppropriateValues() {
        request = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setContentType(ContentType.JSON)
            .addHeader("Authorization", accessToken)
            .build();
    }

    @When("the request contains a valid service token")
    public void theRequestContainsValidServiceToken() {
        request = request.request().header("ServiceAuthorization", validS2sToken);
    }

    @When("the request contains an invalid service token")
    public void theRequestContainsInvalidServiceToken() {
        request = request.request().header("ServiceAuthorization", invalidS2sToken);
    }

    @When("the request contains the {string} as {string}")
    public void theRequestContainsTheAs(String pathParam, String value) {
        given = request.pathParam(pathParam,value);
    }

    @When("the request body contains the {string} as in {string}")
    public void theRequestBodyContainsThe(String description, String fileName) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + fileName));

        String body = new String(b);
        given.body(body);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void callIsSubmittedToTheEndpoint(String resource, String method) {
        given = given().log().all().spec(request);

        Endpoints resourceAPI = Endpoints.valueOf(resource);

        if (method.equalsIgnoreCase("POST")) {
            response = given.when().post(resourceAPI.getResource());
        } else if (method.equalsIgnoreCase("GET")) {
            response = given.when().get(resourceAPI.getResource());
        } else if (method.equalsIgnoreCase("DELETE")) {
            response = given.when().delete(resourceAPI.getResource());
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
            if (responseCode.equalsIgnoreCase("401 Unauthorised")) {
                assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
            } else if (responseCode.equalsIgnoreCase("403 Forbidden")) {
                assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
            }
        }
    }

    @Then("the response returns the matching sitting records")
    public void theResponseReturnsTheMatchingSittingRecords() {
        response.then().assertThat()
            .body("recordCount",equalTo(1))
            .body("sittingRecords[0].sittingDate",equalTo("2023-05-11"))
            .body("sittingRecords[0].statusId",equalTo("RECORDED"))
            .body("sittingRecords[0].regionId",equalTo("1"))
            .body("sittingRecords[0].regionName",equalTo("London"))
            .body("sittingRecords[0].epimmsId",equalTo("1234"))
            .body("sittingRecords[0].hmctsServiceId",equalTo("BBA3"))
            .body("sittingRecords[0].personalCode",equalTo("4918178"))
            .body("sittingRecords[0].personalName",equalTo("Joe Bloggs"))
            .body("sittingRecords[0].contractTypeId",equalTo(1))
            .body("sittingRecords[0].judgeRoleTypeId",equalTo("judge"))
            .body("sittingRecords[0].am",equalTo("AM"))
            .body("sittingRecords[0].pm",equalTo("PM"))
            .body("sittingRecords[0].createdDateTime",equalTo("2023-05-11T17:02:50.000Z"))
            .body("sittingRecords[0].createdByUserId",equalTo("a9ab7f4b-7e0c-49d4-8ed3-75b54d421cdc"));
    }

    @Then("the response contains {string} as {string}")
    public void theResponseContainsAs(String attribute, String value) {
        response.then().assertThat().body(attribute,Matchers.equalTo(value));
    }

    @Then("the {string} is {int}")
    public void theAttributeIs(String attribute, Integer value) {
        response.then().assertThat().body(attribute,equalTo(value));
    }
}
