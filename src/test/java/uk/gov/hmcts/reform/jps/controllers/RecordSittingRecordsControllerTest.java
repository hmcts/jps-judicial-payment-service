package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_LOCATION;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@WebMvcTest(controllers = RecordSittingRecordsController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfiguration.class,
                JwtGrantedAuthoritiesConverter.class})})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class RecordSittingRecordsControllerTest {
    private static final String TEST_SERVICE = "testService";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SittingRecordService sittingRecordService;

    @MockBean
    private JudicialUserDetailsService judicialUserDetailsService;

    @MockBean
    private LocationService regionService;

    @Captor
    private ArgumentCaptor<List<SittingRecordWrapper>> listArgumentCaptor;

    @ParameterizedTest
    @CsvSource({"recordSittingRecordsReplaceDuplicate.json,201,4918178",
        "recordSittingRecords.json,201,4918500"})
    void shouldCreateSittingRecordsWhenRequestIsValid(String fileName,
                                                      int responseCode,
                                                      String personalCode) throws Exception {
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
                jsonPath("$.errorRecords[0].statusId").value(RECORDED.name()),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918179"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value("VALID"),
                jsonPath("$.errorRecords[1].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[1].statusId").value(RECORDED.name()),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918180"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value("VALID"),
                jsonPath("$.errorRecords[2].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[2].statusId").value(RECORDED.name())
            ).andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(responseCode);

        verify(sittingRecordService).checkDuplicateRecords(anyList());
        verify(sittingRecordService).saveSittingRecords(eq(TEST_SERVICE),
                                                        anyList(),
                                                        eq("Recorder"),
                                                        eq("d139a314-eb40-45f4-9e7a-9e13f143cc3a"));
        verify(regionService).setRegionId(eq(TEST_SERVICE),
                                          anyList());
        verify(judicialUserDetailsService, never()).setJudicialUserName(anyList());
    }

    @Test
    void shouldRespondWithBadRequestWhenDuplicateRecordFound() throws Exception {
        LocalDateTime creationDateTime = LocalDateTime.now().minusDays(2)
            .truncatedTo(ChronoUnit.MICROS);
        doAnswer(invocation -> {
            List<SittingRecordWrapper> sittingRecordWrappers = invocation.getArgument(0);
            sittingRecordWrappers.forEach(
                sittingRecordWrapper -> {
                    sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                    sittingRecordWrapper.setCreatedByName("Recorder");
                    sittingRecordWrapper.setCreatedDateTime(creationDateTime);
                    sittingRecordWrapper.setJudgeRoleTypeId("Judge");
                    sittingRecordWrapper.setJudgeRoleTypeName("Tester");
                    sittingRecordWrapper.setAm(Boolean.TRUE);
                    sittingRecordWrapper.setPm(Boolean.FALSE);
                });
            return null;
        }).when(sittingRecordService).checkDuplicateRecords(anyList());

        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message").value("008 could not insert"),
                jsonPath("$.errorRecords[0].postedRecord.sittingDate").value("2022-05-11"),
                jsonPath("$.errorRecords[0].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[0].postedRecord.personalCode").value("4918500"),
                jsonPath("$.errorRecords[0].postedRecord.judgeRoleTypeId").value("Tester"),
                jsonPath("$.errorRecords[0].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[0].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[0].postedRecord.am").value("false"),
                jsonPath("$.errorRecords[0].errorCode").value(POTENTIAL_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[0].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[0].createdDateTime")
                    .value(creationDateTime.toString()),
                jsonPath("$.errorRecords[0].am").value("true"),
                jsonPath("$.errorRecords[0].pm").value("false"),
                jsonPath("$.errorRecords[0].judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[0].judgeRoleTypeName").value("Tester"),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918179"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value(POTENTIAL_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[1].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[1].createdDateTime")
                    .value(creationDateTime.toString()),
                jsonPath("$.errorRecords[1].am").value("true"),
                jsonPath("$.errorRecords[1].pm").value("false"),
                jsonPath("$.errorRecords[1].judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].judgeRoleTypeName").value("Tester"),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918180"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value(POTENTIAL_DUPLICATE_RECORD.name()),
                jsonPath("$.errorRecords[2].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[2].createdDateTime")
                    .value(creationDateTime.toString()),
                jsonPath("$.errorRecords[2].am").value("true"),
                jsonPath("$.errorRecords[2].pm").value("false"),
                jsonPath("$.errorRecords[2].judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].judgeRoleTypeName").value("Tester")
            ).andReturn();

        verify(sittingRecordService).checkDuplicateRecords(anyList());
        verify(sittingRecordService, never()).saveSittingRecords(eq(TEST_SERVICE),
                                                        anyList(),
                                                        eq("Recorder"),
                                                        eq("d139a314-eb40-45f4-9e7a-9e13f143cc3a"));
        verify(regionService).setRegionId(eq(TEST_SERVICE),
                                          anyList());
        verify(judicialUserDetailsService).setJudicialUserName(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue())
            .map(SittingRecordWrapper::getJudgeRoleTypeId)
            .filteredOn(Objects::nonNull)
            .hasSize(3);
    }

    @Test
    void shouldRepondWithBadRequestWhenInvalidLocationRecordFound() throws Exception {
        doAnswer(invocation -> {
            List<SittingRecordWrapper> sittingRecordWrappers = invocation.getArgument(0);
            sittingRecordWrappers.stream()
                .skip(2)
                .forEach(
                    sittingRecordWrapper -> sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD)
                );
            return null;
        }).when(sittingRecordService).checkDuplicateRecords(anyList());

        doAnswer(invocation -> {
            List<SittingRecordWrapper> sittingRecordWrappers = invocation.getArgument(1);
            sittingRecordWrappers.stream()
                .limit(2)
                .forEach(
                    sittingRecordWrapper -> sittingRecordWrapper.setErrorCode(INVALID_LOCATION)
                );
            return null;
        }).when(regionService).setRegionId(anyString(), anyList());

        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message").value("008 could not insert"),
                jsonPath("$.errorRecords[0].postedRecord.sittingDate").value("2022-05-11"),
                jsonPath("$.errorRecords[0].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[0].postedRecord.personalCode").value("4918500"),
                jsonPath("$.errorRecords[0].postedRecord.judgeRoleTypeId").value("Tester"),
                jsonPath("$.errorRecords[0].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[0].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[0].postedRecord.am").value("false"),
                jsonPath("$.errorRecords[0].errorCode").value(INVALID_LOCATION.name()),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918179"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value(INVALID_LOCATION.name()),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimmsId").value("852649"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918180"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value(POTENTIAL_DUPLICATE_RECORD.name())
            ).andReturn();

        verify(sittingRecordService).checkDuplicateRecords(anyList());
        verify(sittingRecordService, never()).saveSittingRecords(eq(TEST_SERVICE),
                                                        anyList(),
                                                        eq("Recorder"),
                                                        eq("d139a314-eb40-45f4-9e7a-9e13f143cc3a"));
        verify(regionService).setRegionId(eq(TEST_SERVICE),
                                          anyList());
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

        verify(sittingRecordService, never()).checkDuplicateRecords(any());
        verify(sittingRecordService, never()).saveSittingRecords(any(), any(), any(), any());
        verify(regionService, never()).setRegionId(any(), any());
    }

    @Test
    @WithMockUser(authorities = {JPS_RECORDER, JPS_SUBMITTER})
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

        verify(sittingRecordService, never()).checkDuplicateRecords(any());
        verify(sittingRecordService, never()).saveSittingRecords(any(), any(), any(), any());
        verify(regionService, never()).setRegionId(any(), any());
    }
}
