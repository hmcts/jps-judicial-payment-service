package uk.gov.hmcts.reform.jps.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SubmitSittingRecordResponse;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;


@WebMvcTest(controllers = SubmitSittingRecordsController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfiguration.class,
                JwtGrantedAuthoritiesConverter.class})})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class SubmitSittingRecordsControllerTest {
    private static final String TEST_SERVICE = "testService";

    @MockBean
    private SittingRecordService sittingRecordService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnRecordCountOfSubmittedRecordsWhenRecordsAreSubmitted() throws Exception {
        when(sittingRecordService.submitSittingRecords(isA(SubmitSittingRecordRequest.class),
                                                       anyString()))
            .thenReturn(SubmitSittingRecordResponse.builder()
                            .recordsSubmitted(3)
                            .recordsClosed(2)
                            .build());

        String requestJson = Resources.toString(getResource("submitSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/submitSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordsSubmitted").value(3),
                jsonPath("$.recordsClosed").value(2)
            );
    }

    @Test
    void shouldThrowWhenRecordsAreSubmitted() throws Exception {
        String requestJson = Resources.toString(getResource("submitSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/submitSittingRecords/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errors[0].fieldName").value("PathVariable"),
                jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory")
            );
    }

    @Test
    void shouldReturn400ResponseWhenPathVariableHmctsServiceCodeNotSet() throws Exception {
        String requestJson = Resources.toString(getResource("submitSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/submitSittingRecords/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errors[0].fieldName").value("PathVariable"),
                jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory")
            );
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        mockMvc.perform(post("/submitSittingRecords/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errors[?(@.fieldName === 'submittedByIdamId')].fieldName").exists(),
                jsonPath("$.errors[?(@.message === 'Submitted by Idam Id is mandatory')].message").exists(),

                jsonPath("$.errors[?(@.fieldName === 'submittedByName')].fieldName").exists(),
                jsonPath("$.errors[?(@.message === 'Submitted by name is mandatory')].message").exists(),

                jsonPath("$.errors[?(@.fieldName === 'regionId')].fieldName").exists(),
                jsonPath("$.errors[?(@.message === 'Region Id is mandatory')].message").exists(),

                jsonPath("$.errors[?(@.fieldName === 'dateRangeTo')].fieldName").exists(),
                jsonPath("$.errors[?(@.message === 'Date range to is mandatory')].message").exists(),

                jsonPath("$.errors[?(@.fieldName === 'dateRangeFrom')].fieldName").exists(),
                jsonPath("$.errors[?(@.message === 'Date range from is mandatory')].message").exists()
            );
    }
}
