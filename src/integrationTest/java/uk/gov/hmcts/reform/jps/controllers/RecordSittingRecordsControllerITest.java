package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.out.RecordSittingRecordResponse;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.DELETE_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_ADMIN;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_PUBLISHER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
public class RecordSittingRecordsControllerITest {
    private static final String TEST_SERVICE = "BBA3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private UserInfo userInfo;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;


    @ParameterizedTest
    @CsvSource({"recordSittingRecordsReplaceDuplicate.json,200,4918178,true",
        "recordSittingRecords.json,201,4918500,false"})
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {JPS_RECORDER, JPS_SUBMITTER})
    void shouldRecordSittingRecordsWhenAllDataIsPresent(String fileName,
                                                        int responseCode,
                                                        String personalCode,
                                                        boolean checkStatusHistory) throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of("jps-recorder"));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        String requestJson = Resources.toString(getResource(fileName), UTF_8);
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                jsonPath("$.errorRecords[0].postedRecord.sittingDate").value("2022-05-11"),
                jsonPath("$.errorRecords[0].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[0].postedRecord.personalCode").value(personalCode),
                jsonPath("$.errorRecords[0].postedRecord.judgeRoleTypeId").value("Tester"),
                jsonPath("$.errorRecords[0].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[0].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[0].postedRecord.am").value("false"),
                jsonPath("$.errorRecords[0].errorCode").value("VALID"),
                jsonPath("$.errorRecords[0].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[0].statusId").value("RECORDED"),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918179"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value("VALID"),
                jsonPath("$.errorRecords[1].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[1].statusId").value("RECORDED"),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918180"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value("VALID"),
                jsonPath("$.errorRecords[2].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[2].statusId").value("RECORDED")
            ).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(responseCode);

        RecordSittingRecordResponse recordSittingRecordResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            RecordSittingRecordResponse.class
        );

        assertThat(recordSittingRecordResponse.getErrorRecords()).describedAs("Created date assertion")
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getCreatedDateTime()));

        if (checkStatusHistory) {
            List<StatusHistory> statusHistories = statusHistoryRepository.findAll();
            assertThat(statusHistories)
                .filteredOn(statusHistory -> statusHistory.getSittingRecord().getId() == 9)
                .extracting("statusId")
                .containsExactlyInAnyOrder(RECORDED, DELETED);
        }
    }


    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {JPS_RECORDER, JPS_SUBMITTER})
    void shouldRepondWithBadRequestWhenDuplicateRecordFound() throws Exception {
        String requestJson =  Resources.toString(getResource("recordSittingRecordsDuplicateRecords.json"), UTF_8);

        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message").value("008 could not insert"),
                jsonPath("$.errorRecords[0].postedRecord.sittingDate").value("2023-05-11"),
                jsonPath("$.errorRecords[0].postedRecord.epimmsId").value("852650"),
                jsonPath("$.errorRecords[0].postedRecord.personalCode").value("4918600"),
                jsonPath("$.errorRecords[0].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[0].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[0].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[0].postedRecord.am").value("false"),
                jsonPath("$.errorRecords[0].errorCode").value(INVALID_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[0].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[0].statusId").value("RECORDED"),
                jsonPath("$.errorRecords[0].createdDateTime").exists(),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimmsId").value("852650"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918600"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Test"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value(POTENTIAL_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[1].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[1].statusId").value("RECORDED"),
                jsonPath("$.errorRecords[1].createdDateTime").exists(),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimmsId").value("852650"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918600"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value(INVALID_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[2].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[2].statusId").value("RECORDED"),
                jsonPath("$.errorRecords[2].createdDateTime").exists()
            );
    }

    @Test
    @WithMockUser(authorities = {JPS_PUBLISHER, JPS_ADMIN})
    void shouldReturnUnauthorizedStatusWhenUserIsUnauthorized() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400ResponseWhenRequestIsEmpty() throws Exception {
        String requestJson = "{}";
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        ModelValidationError actualErrors = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(
            getResource("recordBlankRequestError.json"),
            ModelValidationError.class
        );

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors())
            .hasSameElementsAs(expectedErrors.getErrors());
    }

    @Test
    @WithMockUser(authorities = {"jps-recorder", "jps-submitter"})
    void shouldReturn400WhenHmctsServiceCode() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(status().isBadRequest(),
                          content().contentType(MediaType.APPLICATION_JSON),
                          jsonPath("$.errors[0].fieldName").value("PathVariable"),
                          jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory")
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsByteArray()).isNotNull();
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        String requestJson = Resources.toString(getResource("recordMandatoryFieldsMissing.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        ModelValidationError actualErrors = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(
            getResource("recordMandatoryFieldsMissingErrors.json"),
            ModelValidationError.class
        );

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors())
            .hasSameElementsAs(expectedErrors.getErrors());
    }
}
