package uk.gov.hmcts.reform.jps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class StepDefinitions extends TestVariables {

    RequestSpecification request;
    RequestSpecification given;
    Response response;

    private static boolean isSetupExecuted = false;

    @Before
    public void setup() throws InterruptedException {
        if (!isSetupExecuted) {
            final IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();
            final ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();

            recorderAccessToken = idamTokenGenerator.authenticateUser(recorderUsername, recorderPassword);
            submitterAccessToken = idamTokenGenerator.authenticateUser(submitterUsername, submitterPassword);
            publisherAccessToken = idamTokenGenerator.authenticateUser(publisherUsername, publisherPassword);
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
        } else if (role.equalsIgnoreCase("jps-submitter")) {
            accessToken  = submitterAccessToken;
        } else if (role.equalsIgnoreCase("jps-publisher")) {
            accessToken  = publisherAccessToken;
        } else if (role.equalsIgnoreCase("ccd-import")) {
            accessToken  = invalidAccessToken;
        }
    }

    @Given("a record for the given hmctsServiceCode exists in the database")
    public void recordForTheGivenHmctsServiceCodeExistsInTheDatabase() throws IOException {
        String body = new
            String(Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/F-004_allFields.json")));
        body = body.replace("2023-04-10", randomDate);

        RestAssured.baseURI = testUrl;
        given().header("Content-Type","application/json")
            .header("Authorization", recorderAccessToken)
            .header("ServiceAuthorization", validS2sToken)
            .body(body).log().all()
            .when().post("/recordSittingRecords/ABA5")
            .then().log().all().assertThat().statusCode(201);
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
        String body = new String(Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + fileName)));
        if (description.equalsIgnoreCase("payload matching data from existing record")) {
            body = body.replace("2023-03-10", randomDate);
            body = body.replace("2023-05-12", randomDate);
        }

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

    @When("the request body contains the {string} as {string}")
    public void the_request_body_contains_the_as(String field, String value) {
        given.body(field, ObjectMapperType.valueOf(value));
    }

    @Then("the response has all the fields returned with correct values")
    public void the_response_has_all_the_fields_returned_with_correct_values() {
        response.then().assertThat().body("hmctsServiceCode",equalTo("<hmctsServiceCode>"))
            .body("feeId",equalTo("<feeId>"))
            .body("feeDescription",equalTo("<feeDescription>"))
            .body("judgeRoleTypeId",equalTo("<judgeRoleTypeId>"))
            .body("standardFee",equalTo(1234))
            .body("londonWeightedFee",equalTo(6869));
    }

    @Then("the response is empty")
    public void the_response_is_empty() {
        response.then().assertThat().body("isEmpty()", Matchers.is(true));
    }

    @Then("a {string} response is received with a {string} status code")
    public void responseIsReceivedWithStatusCode(String responseType, String responseCode) {
        response.then().log().all().extract().response().asString();

        if (responseType.equalsIgnoreCase("positive")) {
            if (responseCode.equalsIgnoreCase("200 OK")) {
                assertThat(response.getStatusCode()).isEqualTo(OK.value());
            } else if (responseCode.equalsIgnoreCase("201 Created")) {
                assertThat(response.getStatusCode()).isEqualTo(CREATED.value());
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
            .body("sittingRecords[0].sittingDate",equalTo(randomDate))
            .body("sittingRecords[0].statusId",equalTo("RECORDED"))
            .body("sittingRecords[0].regionId",equalTo("1"))
            .body("sittingRecords[0].regionName",equalTo("London"))
            .body("sittingRecords[0].epimsId",equalTo("229786"))
            .body("sittingRecords[0].hmctsServiceId",equalTo("ABA5"))
            .body("sittingRecords[0].personalCode",equalTo("4918178"))
            .body("sittingRecords[0].personalName",equalTo("Joe Bloggs"))
            .body("sittingRecords[0].contractTypeId",equalTo(2))
            .body("sittingRecords[0].judgeRoleTypeId",equalTo("Judge"))
            .body("sittingRecords[0].am",equalTo("AM"))
            .body("sittingRecords[0].pm",equalTo("PM"));
    }

    @Then("the response contains {string} as {string}")
    public void theResponseContainsAs(String attribute, String value) {
        response.then().assertThat().body(attribute,Matchers.equalTo(value));
    }

    @Then("the response contains {int} {string}")
    public void theResponseContains(Integer expectedSize, String array) {
        response.then().assertThat().body(array,hasSize(expectedSize));
    }

    @Then("the {string} is {int}")
    public void theAttributeIs(String attribute, Integer value) {
        response.then().assertThat().body(attribute,equalTo(value));
    }
}
