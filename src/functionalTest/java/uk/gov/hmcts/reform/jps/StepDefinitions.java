package uk.gov.hmcts.reform.jps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.testUtil.ServiceAuthenticationGenerator;

@Component
@SpringBootTest
public class StepDefinitions {

    @Value("${test-url}")
    protected String testUrl;

//    @Autowired
//    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    RequestSpecification request;
    RequestSpecification given;
    Response response;

//    @Given("a user with the IDAM role of {string}")
//    public void a_user_with_the_idam_role_of(String string) {
//        String solicitorToken = idamTokenGenerator.generateIdamTokenForRecorder();
//
//        System.out.println(solicitorToken);
//
////        request = new RequestSpecBuilder().setBaseUri("http://localhost:3000")
////            .addHeader("Authorization",solicitorToken)
////            .setContentType(ContentType.JSON)
////            .build();
////        given = given().log().all().spec(request);
//    }

    @When("a request is prepared with appropriate values")
    public void a_request_is_prepared_with_appropriate_values() {
    }

    @Given("repo is created")
    public void repo_is_created() {
        ServiceAuthenticationGenerator serviceAuthenticationGenerator = new ServiceAuthenticationGenerator();
        String s2sToken = serviceAuthenticationGenerator.generate();

        System.out.println("heree " + s2sToken);

//        IdamTokenGenerator idamTokenGenerator = new IdamTokenGenerator();
//        String recorderToken = idamTokenGenerator.generateIdamTokenForRecorder();
//
//        System.out.println(recorderToken);

//        request = new RequestSpecBuilder().setBaseUri("http://localhost:3000")
//            .addHeader("Authorization",idamTokenGenerator.generateIdamTokenForRecorder())
//            .setContentType(ContentType.JSON)
//            .build();
//        given = given.log().all().spec(request);

//        System.out.println(recorderToken);
    }

    @When("framework is implemented")
    public void framework_is_implemented() {
    }

    @Then("{string} is printed")
    public void is_printed(String string) {
        System.out.println(string);
    }
}
