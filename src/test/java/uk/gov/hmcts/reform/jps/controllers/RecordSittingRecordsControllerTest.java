package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

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
    private LocationService regionService;

    @Test
    void shouldCreateSittingRecordsWhenRequestIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.errorRecords[0].postedRecord.sittingDate").value("2023-05-11"),
                jsonPath("$.errorRecords[0].postedRecord.epimsId").value("852649"),
                jsonPath("$.errorRecords[0].postedRecord.personalCode").value("4918178"),
                jsonPath("$.errorRecords[0].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[0].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[0].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[0].postedRecord.am").value("false"),
                jsonPath("$.errorRecords[0].errorCode").value("VALID"),
                jsonPath("$.errorRecords[0].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[0].statusId").value(StatusId.RECORDED.name()),

                jsonPath("$.errorRecords[1].postedRecord.sittingDate").value("2023-04-10"),
                jsonPath("$.errorRecords[1].postedRecord.epimsId").value("852649"),
                jsonPath("$.errorRecords[1].postedRecord.personalCode").value("4918178"),
                jsonPath("$.errorRecords[1].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[1].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[1].postedRecord.pm").value("false"),
                jsonPath("$.errorRecords[1].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[1].errorCode").value("VALID"),
                jsonPath("$.errorRecords[1].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[1].statusId").value(StatusId.RECORDED.name()),

                jsonPath("$.errorRecords[2].postedRecord.sittingDate").value("2023-03-09"),
                jsonPath("$.errorRecords[2].postedRecord.epimsId").value("852649"),
                jsonPath("$.errorRecords[2].postedRecord.personalCode").value("4918178"),
                jsonPath("$.errorRecords[2].postedRecord.judgeRoleTypeId").value("Judge"),
                jsonPath("$.errorRecords[2].postedRecord.contractTypeId").value("1"),
                jsonPath("$.errorRecords[2].postedRecord.pm").value("true"),
                jsonPath("$.errorRecords[2].postedRecord.am").value("true"),
                jsonPath("$.errorRecords[2].errorCode").value("VALID"),
                jsonPath("$.errorRecords[2].createdByName").value("Recorder"),
                jsonPath("$.errorRecords[2].statusId").value(StatusId.RECORDED.name())
            ).andReturn();
    }

    @Test
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
