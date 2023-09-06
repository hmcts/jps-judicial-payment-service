package uk.gov.hmcts.reform.jps.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@Transactional
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
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {"jps-submitter"})
    void shouldReturnRecordCountOfSubmittedRecordsWhenRecordsAreSubmitted(
        String regionId,
        Integer submitted,
        Integer closed,
        StatusId statusId,
        StatusId previousId,
        Integer count) throws Exception {
        HashSet<StatusId> statusIds = new HashSet<>();
        statusIds.add(statusId);
        statusIds.add(previousId);

        String requestJson = Resources.toString(getResource("submitSittingRecordsWithDynamicRegionId.json"), UTF_8);
        String updatedJson = requestJson.replace("replaceRegion", regionId);
        mockMvc.perform(post("/submitSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedJson))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordsSubmitted").value(submitted),
                jsonPath("$.recordsClosed").value(closed)
            );

        List<SittingRecord> sittingRecords = sittingRecordRepository.findAll();
        assertThat(sittingRecords)
            .filteredOn(sittingRecord -> sittingRecord.getRegionId().equals(regionId))
            .map(SittingRecord::getStatusId)
            .contains(statusId);

        List<StatusHistory> statusHistories = statusHistoryRepository.findAll();

        assertThat(statusHistories)
            .filteredOn(statusHistory -> statusHistory.getSittingRecord().getRegionId().equals(regionId))
            .map(StatusHistory::getStatusId)
            .hasSize(count)
            .containsExactlyInAnyOrderElementsOf(statusIds);
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
