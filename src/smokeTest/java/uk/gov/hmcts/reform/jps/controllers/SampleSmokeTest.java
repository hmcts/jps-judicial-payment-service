package uk.gov.hmcts.reform.jps.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;

@ExtendWith(SpringExtension.class)
class SampleSmokeTest {

    @Value("${TEST_URL:http://localhost:4550}")
    private String testUrl;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void smokeTest() {
        Response response = given()
            .log().all()
            .contentType(ContentType.JSON)
            .when()
            .get()
            .then()
            .log().all()
            .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Welcome to jps-judicial-payment-service",
                                response.asString());
    }
}
