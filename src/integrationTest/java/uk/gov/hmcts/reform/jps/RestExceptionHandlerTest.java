package uk.gov.hmcts.reform.jps;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.jps.controllers.TestController;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.exceptions.ServiceException;
import uk.gov.hmcts.reform.jps.exceptions.UnauthorisedException;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
public class RestExceptionHandlerTest extends BaseTest {

    public static String ERROR_PATH_ERROR = "$.errors";
    public static String ERROR_PATH_STATUS = "$.status";
    public static String testExceptionMessage = "test message";

    @MockBean
    protected TestController service;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("should return correct response when FeignException is thrown")
    @Test
    void shouldHandleFeignException() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "url",
            new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.NotFound(testExceptionMessage, request, null, null))
            .when(service).getHearing();

        ResultActions result =  this.mockMvc.perform(get("/test")
            .contentType(MediaType.APPLICATION_JSON));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
            "INTERNAL_SERVER_ERROR");
    }

    @DisplayName("should return correct response when ResourceNotFoundException is thrown")
    @Test
    void shouldHandleResourceNotFoundException() throws Exception {

        // WHEN
        Mockito.doThrow(new ResourceNotFoundException(testExceptionMessage)).when(service).getHearing();

        ResultActions result =  this.mockMvc.perform(get("/test")
            .contentType(MediaType.APPLICATION_JSON));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.FORBIDDEN.value(), testExceptionMessage, "FORBIDDEN");
    }

    @DisplayName("should return correct response when ServiceException is thrown")
    @Test
    void shouldHandleServiceException() throws Exception {
        // WHEN
        Mockito.doThrow(new ServiceException(testExceptionMessage)).when(service).getHearing();

        ResultActions result =  this.mockMvc.perform(get("/test")
            .contentType(MediaType.APPLICATION_JSON));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
            "INTERNAL_SERVER_ERROR");
    }

    @DisplayName("should return correct response when unauthorised is thrown")
    @Test
    void shouldHandleUnauthorisedException() throws Exception {
        // WHEN
        Mockito.doThrow(new UnauthorisedException(testExceptionMessage)).when(service).getHearing();

        ResultActions result =  this.mockMvc.perform(get("/test")
            .contentType(MediaType.APPLICATION_JSON));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.UNAUTHORIZED.value(), testExceptionMessage,
            "UNAUTHORIZED");
    }

    private void assertHttpErrorResponse(ResultActions result, int expectedStatusCode, String expectedMessage,
                                         String expectedStatus) throws Exception {

        result
            .andExpect(status().is(expectedStatusCode))
            .andExpect(jsonPath(ERROR_PATH_STATUS).value(expectedStatus))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value(expectedMessage));
    }
}