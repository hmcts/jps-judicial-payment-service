package uk.gov.hmcts.reform.jps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import uk.gov.hmcts.reform.jps.config.Endpoints;
import uk.gov.hmcts.reform.jps.config.TestVariables;
import uk.gov.hmcts.reform.jps.testutils.IdamTokenGenerator;
import uk.gov.hmcts.reform.jps.testutils.RandomDateGenerator;
import uk.gov.hmcts.reform.jps.testutils.ServiceAuthenticationGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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

            IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();
            recorderAccessToken = idamTokenGenerator.authenticateUser(recorderUsername, recorderPassword);
            submitterAccessToken = idamTokenGenerator.authenticateUser(submitterUsername, submitterPassword);
            publisherAccessToken = idamTokenGenerator.authenticateUser(publisherUsername, publisherPassword);
            adminAccessToken = idamTokenGenerator.authenticateUser(adminUsername, adminPassword);
            johAdminAccessToken = idamTokenGenerator.authenticateUser(johAdminUsername, johAdminPassword);
            invalidAccessToken = idamTokenGenerator.authenticateUser(invalidUsername, invalidPassword);

            ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();
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
        } else if (role.equalsIgnoreCase("jps-admin")) {
            accessToken  = adminAccessToken;
        } else if (role.equalsIgnoreCase("jps-joh-admin")) {
            accessToken  = johAdminAccessToken;
        } else if (role.equalsIgnoreCase("ccd-import")) {
            accessToken  = invalidAccessToken;
        }
    }

    @Given("a record for the given hmctsServiceCode exists in the database with the payload {string}")
    public void recordForTheGivenHmctsServiceCodeExistsInTheDatabaseWithThePayload(String payload) throws IOException {
        randomDate = RandomDateGenerator.generateRandomDate().toString();

        String body = new
            String(Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + payload + ".json")));
        body = body.replace("2023-04-10", randomDate);

        RestAssured.baseURI = testUrl;
        given().header("Content-Type","application/json")
            .header("Authorization", recorderAccessToken)
            .header("ServiceAuthorization", validS2sToken)
            .body(body).log().all()
            .when().post("/recordSittingRecords/ABA5")
            .then().log().all().assertThat().statusCode(201);
    }

    @Given("a search is done on the hmctsServiceCode {string}, with the payload {string} to get the {string}")
    public void searchIsDoneOnTheHmctsServiceCodeWithThePayloadToGetThe(String serviceCode, String payload, String
        attribute) throws IOException {
        String body = new
            String(Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + payload + ".json")));
        body = body.replace("2023-03-10", randomDate);

        RestAssured.baseURI = testUrl;
        ValidatableResponse response = given().header("Content-Type", "application/json")
            .header("Authorization", recorderAccessToken)
            .header("ServiceAuthorization", validS2sToken)
            .body(body).log().all()
            .when().post("/sitting-records/searchSittingRecords/" + serviceCode)
            .then().log().all().assertThat().statusCode(200);

        recordAttribute = propertiesReader.getJsonPath(response, attribute);
    }

    @Given("the existing record is in Submitted state")
    public void theExistingRecordIsInSubmittedState() {
        // Write code here after IJPS-62 is ready
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
        if (value.equalsIgnoreCase("id of the previously created record")) {
            value = recordAttribute;
        }

        given = request.pathParam(pathParam,value);
    }

    @When("the request body contains the {string} as in {string}")
    public void theRequestBodyContainsThe(String description, String fileName) throws IOException {
        String body = new String(Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/" + fileName
                                                                  + ".json")));
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
            } else if (responseCode.equalsIgnoreCase("409 Conflict")) {
                assertThat(response.getStatusCode()).isEqualTo(CONFLICT.value());
            } else if (responseCode.equalsIgnoreCase("404 Not Found")) {
                assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND.value());
            }
        }
    }

    @Then("the response returns the matching sitting records")
    public void theResponseReturnsTheMatchingSittingRecords() {
        response.then().assertThat()
            .body("recordCount",equalTo(1))
            .body("recordingUsers[0].userId",equalTo("d139a314-eb40-45f4-9e7a-9e13f143cc3a"))
            .body("recordingUsers[0].userName",equalTo("Recorder"))
            .body("sittingRecords[0].sittingRecordId",Matchers.notNullValue())
            .body("sittingRecords[0].sittingDate",equalTo(randomDate))
            .body("sittingRecords[0].statusId",equalTo("RECORDED"))
            .body("sittingRecords[0].regionId",equalTo("1"))
            .body("sittingRecords[0].regionName",equalTo("London"))
            .body("sittingRecords[0].epimmsId",equalTo("229786"))
            .body("sittingRecords[0].venueName",equalTo("Barnet"))
            .body("sittingRecords[0].hmctsServiceId",equalTo("ABA5"))
            .body("sittingRecords[0].personalCode",equalTo("4918178"))
            .body("sittingRecords[0].personalName",equalTo("Joe Bloggs"))
            .body("sittingRecords[0].contractTypeId",equalTo(2))
            .body("sittingRecords[0]", Matchers.hasKey("contractTypeName"))
            .body("sittingRecords[0].judgeRoleTypeId",equalTo("Judge"))
            .body("sittingRecords[0]", Matchers.hasKey("judgeRoleTypeName"))
            .body("sittingRecords[0].crownServantFlag",equalTo(false))
            .body("sittingRecords[0].londonFlag",equalTo(false))
            .body("sittingRecords[0]", Matchers.hasKey("payrollId"))
            .body("sittingRecords[0]", Matchers.hasKey("accountCode"))
            .body("sittingRecords[0]", Matchers.hasKey("fee"))
            .body("sittingRecords[0].am",equalTo(true))
            .body("sittingRecords[0].pm",equalTo(true))
            .body("sittingRecords[0].createdDateTime",Matchers.notNullValue())
            .body("sittingRecords[0].createdByUserId",equalTo("d139a314-eb40-45f4-9e7a-9e13f143cc3a"))
            .body("sittingRecords[0].createdByUserName",equalTo("Recorder"))
            .body("sittingRecords[0].changedDateTime",Matchers.notNullValue())
            .body("sittingRecords[0].changedByUserId",equalTo("d139a314-eb40-45f4-9e7a-9e13f143cc3a"))
            .body("sittingRecords[0].changedByUserName",equalTo("Recorder"));
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
