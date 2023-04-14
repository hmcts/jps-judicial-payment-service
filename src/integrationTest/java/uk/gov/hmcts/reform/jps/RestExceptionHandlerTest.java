package uk.gov.hmcts.reform.jps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
public class RestExceptionHandlerTest extends BaseTest {

    public static String ERROR_PATH_ERROR = "$.errors";
    public static String ERROR_PATH_STATUS = "$.status";
    public static String testExceptionMessage = "test message";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("should return correct response when BadRequestException is thrown")
    @Test
    @Disabled
    void shouldHandleBadRequestException() throws Exception {

        /// WHEN
        //Mockito.doThrow(new BadRequestException(testExceptionMessage)).when(service)
        //    .validateHearingRequest(any(HearingRequest.class));
        //
        //ResultActions result =  this.mockMvc.perform(post("/hearing")
        //    .contentType(MediaType.APPLICATION_JSON)
        //    .content(objectMapper.writeValueAsString(validRequest)));
        //
        //// THEN
        //assertHttpErrorResponse(result, HttpStatus.BAD_REQUEST.value(), testExceptionMessage, "BAD_REQUEST");
    }

    @DisplayName("should return correct response when FeignException is thrown")
    @Test
    @Disabled
    void shouldHandleFeignException() throws Exception {
        //Request request = Request.create(Request.HttpMethod.GET, "url",
        //    new HashMap<>(), null, new RequestTemplate());
        //Mockito.doThrow(new FeignException.NotFound(testExceptionMessage, request, null))
        //    .when(service).verifyAccess(anyString());
        //
        //ResultActions result =  this.mockMvc.perform(post("/hearing")
        //    .contentType(MediaType.APPLICATION_JSON)
        //    .content(objectMapper.writeValueAsString(validRequest)));
        //
        //// THEN
        //assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
        //    "INTERNAL_SERVER_ERROR");
    }

    @DisplayName("should return correct response when ResourceNotFoundException is thrown")
    @Test
    @Disabled
    void shouldHandleResourceNotFoundException() throws Exception {

        ///// WHEN
        //Mockito.doThrow(new ResourceNotFoundException(testExceptionMessage)).when(service).verifyAccess(anyString());
        //
        //ResultActions result =  this.mockMvc.perform(post("/hearing")
        //    .contentType(MediaType.APPLICATION_JSON)
        //    .content(objectMapper.writeValueAsString(validRequest)));
        //
        //// THEN
        //assertHttpErrorResponse(result, HttpStatus.FORBIDDEN.value(), testExceptionMessage, "FORBIDDEN");
    }

    @DisplayName("should return correct response when ServiceException is thrown")
    @Test
    @Disabled
    void shouldHandleServiceException() throws Exception {

        /// WHEN
        //Mockito.doThrow(new ServiceException(testExceptionMessage)).when(service).verifyAccess(anyString());
        //
        //ResultActions result =  this.mockMvc.perform(post("/hearing")
        //    .contentType(MediaType.APPLICATION_JSON)
        //    .content(objectMapper.writeValueAsString(validRequest)));
        //
        //// THEN
        //assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
        //    "INTERNAL_SERVER_ERROR");
    }

    private void assertHttpErrorResponse(ResultActions result, int expectedStatusCode, String expectedMessage,
                                         String expectedStatus) throws Exception {

        result
            .andExpect(status().is(expectedStatusCode))
            .andExpect(jsonPath(ERROR_PATH_STATUS).value(expectedStatus))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value(expectedMessage));
    }
}