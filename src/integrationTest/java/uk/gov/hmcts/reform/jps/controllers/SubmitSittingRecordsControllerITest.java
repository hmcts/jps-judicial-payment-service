package uk.gov.hmcts.reform.jps.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.google.common.io.Resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_SERVICE_TEST_DATA;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
class SubmitSittingRecordsControllerITest {
    private static final String TEST_SERVICE = "BBA3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SittingRecordRepository sittingRecordRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;


    @ParameterizedTest
    @CsvSource(textBlock = """
      # RegionId,    Submitted,   Closed, StatusId, PreviousStatusId, Count
      6,             1,           0,      SUBMITTED, RECORDED,          2
      7,             0,           1,      CLOSED,    RECORDED,          2
      8,             0,           1,      CLOSED,    RECORDED,          2
      9,             1,           0,      SUBMITTED, RECORDED,          2
      10,            0,           0,      RECORDED,  RECORDED,          1
      11,            0,           0,      RECORDED , RECORDED,          1
        """)
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY, INSERT_SERVICE_TEST_DATA})
    @WithMockUser(authorities = {"jps-submitter"})
    void shouldReturnRecordCountOfSubmittedRecordsWhenRecordsAreSubmitted() throws Exception {
        String requestJson = Resources.toString(getResource("submitSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/submitSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordsSubmitted").value(1)
            );
    }

    @Test
    @WithMockUser(authorities = {"jps-submitter"})
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
    @WithMockUser(authorities = {"jps-submitter"})
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
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE_TEST_DATA})
    @WithMockUser(authorities = {JPS_RECORDER, JPS_SUBMITTER})
    void shouldReturn400ResponseWhenServiceNotOnboarded() throws Exception {
        String requestJson = Resources.toString(getResource("submitSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/submitSittingRecords/{hmctsServiceCode}", "CBA5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(status().isBadRequest(),
                          content().contentType(MediaType.APPLICATION_JSON),
                          jsonPath("$.errors[0].fieldName").value("hmctsServiceCode"),
                          jsonPath("$.errors[0].message").value("004 unknown hmctsServiceCode")
            )
            .andReturn();
    }

    @Test
    @WithMockUser(authorities = {"jps-submitter"})
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        mockMvc.perform(post("/submitSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
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
