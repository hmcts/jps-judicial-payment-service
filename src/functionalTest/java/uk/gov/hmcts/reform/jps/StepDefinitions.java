package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import uk.gov.hmcts.reform.hmc.jp.functional.resources.APIResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static  org.hamcrest.Matchers.*;

import static org.springframework.http.HttpStatus.*;

public class StepDefinitions {

    RequestSpecification request;
    RequestSpecification given;
    Response response;

    @Given("a user with the IDAM role of {string}")
    public void a_user_with_the_idam_role_of(String string) {
        System.out.println(string);
    }

    @When("a request is prepared with appropriate values")
    public void a_request_is_prepared_with_appropriate_values() {
        request = new RequestSpecBuilder().setBaseUri("http://localhost:3000").setContentType(
            ContentType.JSON).build();
        given = given().log().all().spec(request);
    }

    @When("a request is missing the S2S token")
    public void a_request_is_missing_the_s2s_token() {
        request = new RequestSpecBuilder().setBaseUri("http://localhost:3000").setContentType(
            ContentType.JSON).build();
        given = given().log().all().spec(request);
    }

    @When("the request contains the {string} as {string}")
    public void the_request_contains_the_as(String pathParam, String value) {
        given = given().log().all().pathParam(pathParam,value).spec(request);
    }

    @When("a call is submitted to the {string} endpoint using a {string} request")
    public void a_call_is_submitted_to_the_get_fee_endpoint(String resource, String method) {
        APIResources resourceAPI = APIResources.valueOf(resource);

        if (method.equalsIgnoreCase("POST"))
            response = given.when().post(resourceAPI.getResource());
        else if (method.equalsIgnoreCase("GET"))
            response = given.when().get(resourceAPI.getResource());
        else if (method.equalsIgnoreCase("DELETE"))
            response = given.when().delete(resourceAPI.getResource());
    }

    @When("the request contains the additional header {string} as {string}")
    public void the_request_contains_the_additional_header_as(String header, String value) {
        given.header(header,value);
    }

    @When("the request body contains the {string} as in {string}")
    public void the_request_body_contains_the(String description, String fileName) throws IOException {

        byte[] b = Files.readAllBytes(Paths.get("./src/functionalTest/resources/payloads/"+fileName));

        String body = new String(b);
        given.body(body).then().log().all();
    }

    @Then("the response contains a new feeId")
    public void the_response_contains_a_new_fee_id() {
        response.then().assertThat().body("feeId", notNullValue());
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

    @Then("the response contains {string} as {string}")
    public void the_response_contains_as(String attribute, String value) {
        response.then().assertThat().body(attribute,equalTo(value));
    }

    @Then("the response is empty")
    public void the_response_is_empty() {
        response.then().assertThat().body("isEmpty()", Matchers.is(true));
    }

    @Then("a {string} response is received with a {string} status code")
    public void a_response_is_received_with_a_status_code(String responseType, String responseCode) {

        response.then().log().all().extract().response().asString();

        if (responseType.equalsIgnoreCase("positive")) {
            if (responseCode.equalsIgnoreCase("200 OK"))
                assertThat(response.getStatusCode()).isEqualTo(OK.value());
        }
        else {
            if (responseCode.equalsIgnoreCase("400 Bad Request"))
                assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
            else if (responseCode.equalsIgnoreCase("401 Unauthorised"))
                assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
            else if (responseCode.equalsIgnoreCase("403 Forbidden"))
                assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
        }
    }
}
