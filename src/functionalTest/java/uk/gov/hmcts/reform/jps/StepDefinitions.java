package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import uk.gov.hmcts.reform.jps.config.APIResources;
import uk.gov.hmcts.reform.jps.config.PropertiesReader;
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
        String s2sToken = serviceAuthenticationGenerator.generate();
        request = request.request().header("ServiceAuthorization", s2sToken);
        //given = given.header("ServiceAuthorization", s2sToken).log().all().spec(request);
    }

    @When("the request contains an invalid service token")
    public void theRequestContainsInvalidServiceToken() {
        String s2sToken = serviceAuthenticationGenerator.generate("xui_webapp");
        request = request.request().header("ServiceAuthorization", s2sToken);
        // given = given.header("ServiceAuthorization", s2sToken);
    }

    @When("the request contains the {string} as {string}")
    public void theRequestContainsTheAs(String pathParam, String value) {
        given = given().log().all().pathParam(pathParam,value).spec(request);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void callIsSubmittedToTheEndpoint(String resource, String method) {
        APIResources resourceAPI = APIResources.valueOf(resource);

        if (method.equalsIgnoreCase("POST")) {
            response = given.when().post(resourceAPI.getResource());
        } else if (method.equalsIgnoreCase("GET")) {
            response = given.when().get(resourceAPI.getResource());
        } else if (method.equalsIgnoreCase("DELETE")) {
            response = given.when().delete(resourceAPI.getResource());
        }
    }

    @When("the request body contains the {string} as in {string}")
    public void theRequestBodyContainsThe(String description, String fileName) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + fileName));

        String body = new String(b);
        given.body(body);
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
            .body("sittingRecords[0].epimsId",equalTo("1234"))
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
