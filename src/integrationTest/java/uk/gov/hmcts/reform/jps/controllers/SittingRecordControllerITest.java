package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.errors.FieldError;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.DELETE_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_ADMIN;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
class SittingRecordControllerITest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private UserInfo userInfo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SittingRecordRepository recordRepository;

    @BeforeEach
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY})

    @Test
    void shouldHaveOkResponseWhenRequestIsValidAndNoMatchingRecord() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        String updatedRecord = requestJson.replace("toDate", LocalDate.now().toString());
        mockMvc
            .perform(post("/sitting-records/searchSittingRecords/{hmctsServiceCode}", "2")
              .contentType(MediaType.APPLICATION_JSON)
              .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("0"),
                jsonPath("$.sittingRecords").isEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidAndHasMatchingRecords() throws Exception {

        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(RECORDED)
            .regionId("1")
            .epimmsId("123")
            .hmctsServiceId("BBA3")
            .personalCode("4923421")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .build();

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertThat(persistedSittingRecord).isEqualTo(sittingRecord);

        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        String updatedRecord = requestJson.replace("toDate", LocalDate.now().toString());
        mockMvc
            .perform(post("/sitting-records/searchSittingRecords/{hmctsServiceCode}", "BBA3")
              .contentType(MediaType.APPLICATION_JSON)
              .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("1"),
                jsonPath("$.sittingRecords[0].sittingRecordId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingDate").isNotEmpty(),
                jsonPath("$.sittingRecords[0].statusId").value("RECORDED"),
                jsonPath("$.sittingRecords[0].regionId").value("1"),
                jsonPath("$.sittingRecords[0].regionName").value("London"),
                jsonPath("$.sittingRecords[0].epimmsId").value("123"),
                jsonPath("$.sittingRecords[0].hmctsServiceId").value("BBA3"),
                jsonPath("$.sittingRecords[0].personalCode").value("4923421"),
                jsonPath("$.sittingRecords[0].personalName").value("Joe Bloggs"),
                jsonPath("$.sittingRecords[0].judgeRoleTypeId").value("HighCourt"),
                jsonPath("$.sittingRecords[0].am").value("AM"),
                jsonPath("$.sittingRecords[0].pm").isEmpty(),
                jsonPath("$.sittingRecords[0].createdDateTime").isEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserId").isEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserName").isEmpty(),
                jsonPath("$.sittingRecords[0].changeDateTime").isEmpty(),
                jsonPath("$.sittingRecords[0].changeByUserId").isEmpty(),
                jsonPath("$.sittingRecords[0].changeByUserName").isEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecordsWithoutMandatoryFields.json"), UTF_8);
        MvcResult response = mockMvc
            .perform(post("/sitting-records/searchSittingRecords/{hmctsServiceCode}", "BBA3")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        byte[] responseBody = response.getResponse().getContentAsByteArray();

        ModelValidationError actualErrors = objectMapper.readValue(
            responseBody,
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(getResource("mandatoryFieldError.json"),
                               ModelValidationError.class);

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors()).containsAll(expectedErrors.getErrors());
    }

    @Test
    void shouldReturn400ResponseWhenEnumValuesIncorrect() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecordsInValidEnumData.json"), UTF_8);
        MvcResult response = mockMvc
            .perform(post("/sitting-records/searchSittingRecords/{hmctsServiceCode}", "2")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        byte[] responseBody = response.getResponse().getContentAsByteArray();

        ModelValidationError actualErrors = objectMapper.readValue(
            responseBody,
            ModelValidationError.class
        );

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors().size()).isEqualTo(1);

        FieldError fieldError = actualErrors.getErrors().get(0);
        assertThat(fieldError.getFieldName())
            .isEqualTo("RequestNotReadable");
        assertThat(fieldError.getMessage())
            .contains("one of the values accepted for Enum class: [ASCENDING, DESCENDING]");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
      # ROLE
      jps-recorder
        """)
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {JPS_RECORDER})
    void shouldDeleteSittingRecordWhenSittingRecordPresentRecorder(String role) throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of(role));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 3))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
      # ROLE
      jps-submitter
        """)
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {JPS_SUBMITTER})
    void shouldDeleteSittingRecordWhenSittingRecordPresentSubmitter(String role) throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of(role));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
      # ROLE
      jps-admin
        """)
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {JPS_ADMIN})
    void shouldDeleteSittingRecordWhenSittingRecordPresentAdmin(String role) throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of(role));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 4))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {JPS_RECORDER})
    void shouldThrowSittingRecordNotFoundWhenSittingRecordNotFoundInDb() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2000))
            .andDo(print())
            .andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value("NOT_FOUND"),
                jsonPath("$.errors").value("Sitting Record ID Not Found")
            );
    }

}
