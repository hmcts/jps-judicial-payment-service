package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.out.errors.FieldError;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;


class SittingRecordControllerITest extends BaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StatusHistoryRepository historyRepository;

    @Autowired
    private SittingRecordRepository recordRepository;

    private static final String SEARCH_SITTING_RECORDS_JSON = "searchSittingRecords.json";
    private static final String SEARCH_SITTING_RECORDS_NO_EPIMMS_JSON = "searchSittingRecordsNoEpimms.json";
    private static final String SEARCH_SITTING_RECORDS_NO_REGION_JSON = "searchSittingRecordsNoRegion.json";
    private static final String SEARCH_SITTING_RECORDS_NO_EPIMMS_NO_REGION_JSON =
        "searchSittingRecordsNoEpimmsNoRegion.json";

    private static final String DENZEL_WASHINGTON = "Denzel Washington";
    private static final String JASON_BOURNE = "Jason Bourne";
    private static final String JOE_BLOGGS = "Joe Bloggs";
    private static final String JACKIE_CHAN = "Jackie Chan";
    private static final String HIGHCOURT = "HighCourt";
    private static final String LONDON = "London";

    public static final String SEARCH_URL = "/sitting-records/searchSittingRecords/{hmctsServiceCode}";

    public static final String TO_DATE_CONST = "toDate";
    public static final String RECORDING_USERS_JSON_CONST = "$.recordingUsers";


    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        recordRepository.deleteAll();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidAndNoMatchingRecord() throws Exception {
        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "2")
              .contentType(MediaType.APPLICATION_JSON)
              .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("0"),
                jsonPath(RECORDING_USERS_JSON_CONST).exists(),
                jsonPath(RECORDING_USERS_JSON_CONST).isArray(),
                jsonPath(RECORDING_USERS_JSON_CONST).isEmpty(),
                jsonPath("$.sittingRecords").isEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidAndHasMatchingRecords() throws Exception {

        SittingRecord sittingRecord = createSittingRecord(2L, "123", "BBA3",
                                                          HIGHCOURT, "4923421", "1",
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory1 = createStatusHistory(JASON_BOURNE, "11233",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord.addStatusHistory(statusHistory1);
        sittingRecord = recordRepository.save(sittingRecord);
        sittingRecord.getFirstStatusHistory();

        StatusHistory statusHistory2 = createStatusHistory(JACKIE_CHAN, "11255",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord.addStatusHistory(statusHistory2);
        assertEquals(statusHistory2.getStatusId(), sittingRecord.getStatusId());
        historyRepository.save(statusHistory2);
        sittingRecord = recordRepository.save(sittingRecord);

        StatusHistory statusHistory3 = createStatusHistory(DENZEL_WASHINGTON, "11266",
                                                           LocalDateTime.now(), StatusId.PUBLISHED.name());
        sittingRecord.addStatusHistory(statusHistory3);
        assertEquals(statusHistory3.getStatusId(), sittingRecord.getStatusId());
        statusHistory3 = historyRepository.save(statusHistory3);
        sittingRecord = recordRepository.save(sittingRecord);

        SittingRecord persistedSittingRecord = recordRepository.findAll().get(0);
        assertEquals(statusHistory3.getStatusId(), persistedSittingRecord.getStatusId());

        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertEquals(persistedSittingRecord.getStatusHistories().get(0), sittingRecord.getStatusHistories().get(0));
        assertEquals(persistedSittingRecord.getStatusHistories().get(1), sittingRecord.getStatusHistories().get(1));

        assertThat(persistedSittingRecord.equalsDomainObject(sittingRecord));

        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "BBA3")
              .contentType(MediaType.APPLICATION_JSON)
              .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("1"),
                jsonPath(RECORDING_USERS_JSON_CONST).exists(),
                jsonPath(RECORDING_USERS_JSON_CONST).isArray(),
                jsonPath(RECORDING_USERS_JSON_CONST).isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingRecordId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingDate").isNotEmpty(),
                jsonPath("$.sittingRecords[0].statusId").value(StatusId.PUBLISHED.name()),
                jsonPath("$.sittingRecords[0].regionId").value("1"),
                jsonPath("$.sittingRecords[0].regionName").value(LONDON),
                jsonPath("$.sittingRecords[0].epimsId").value("123"),
                jsonPath("$.sittingRecords[0].hmctsServiceId").value("BBA3"),
                jsonPath("$.sittingRecords[0].personalCode").value("4923421"),
                jsonPath("$.sittingRecords[0].personalName").value(JOE_BLOGGS),
                jsonPath("$.sittingRecords[0].judgeRoleTypeId").value(HIGHCOURT),
                jsonPath("$.sittingRecords[0].am").value("AM"),
                jsonPath("$.sittingRecords[0].pm").isEmpty(),
                jsonPath("$.sittingRecords[0].createdDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserName").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserName").isNotEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidButNoRegionAndHasMatchingRecords() throws Exception {

        SittingRecord sittingRecord = createSittingRecord(2L, "123", "BBA3",
                                                          HIGHCOURT, "4923421", "1",
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory1 = createStatusHistory(JASON_BOURNE, "11233",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord.addStatusHistory(statusHistory1);
        sittingRecord = recordRepository.save(sittingRecord);
        sittingRecord.getFirstStatusHistory();

        StatusHistory statusHistory2 = createStatusHistory(JACKIE_CHAN, "11255",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord.addStatusHistory(statusHistory2);
        assertEquals(statusHistory2.getStatusId(), sittingRecord.getStatusId());
        historyRepository.save(statusHistory2);
        sittingRecord = recordRepository.save(sittingRecord);

        StatusHistory statusHistory3 = createStatusHistory(DENZEL_WASHINGTON, "11266",
                                                           LocalDateTime.now(), StatusId.PUBLISHED.name());
        sittingRecord.addStatusHistory(statusHistory3);
        assertEquals(statusHistory3.getStatusId(), sittingRecord.getStatusId());
        statusHistory3 = historyRepository.save(statusHistory3);
        sittingRecord = recordRepository.save(sittingRecord);

        SittingRecord persistedSittingRecord = recordRepository.findAll().get(0);
        assertEquals(statusHistory3.getStatusId(), persistedSittingRecord.getStatusId());

        assertNotNull(persistedSittingRecord);
        assertNotNull(persistedSittingRecord.getId());
        assertEquals(persistedSittingRecord.getStatusHistories().get(0), sittingRecord.getStatusHistories().get(0));
        assertEquals(persistedSittingRecord.getStatusHistories().get(1), sittingRecord.getStatusHistories().get(1));

        assertTrue(persistedSittingRecord.equalsDomainObject(sittingRecord));

        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_NO_REGION_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "BBA3")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("1"),
                jsonPath(RECORDING_USERS_JSON_CONST).exists(),
                jsonPath(RECORDING_USERS_JSON_CONST).isArray(),
                jsonPath(RECORDING_USERS_JSON_CONST).isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingRecordId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingDate").isNotEmpty(),
                jsonPath("$.sittingRecords[0].statusId").value(StatusId.PUBLISHED.name()),
                jsonPath("$.sittingRecords[0].regionId").value("1"),
                jsonPath("$.sittingRecords[0].regionName").value(LONDON),
                jsonPath("$.sittingRecords[0].epimsId").value("123"),
                jsonPath("$.sittingRecords[0].hmctsServiceId").value("BBA3"),
                jsonPath("$.sittingRecords[0].personalCode").value("4923421"),
                jsonPath("$.sittingRecords[0].personalName").value(JOE_BLOGGS),
                jsonPath("$.sittingRecords[0].judgeRoleTypeId").value(HIGHCOURT),
                jsonPath("$.sittingRecords[0].am").value("AM"),
                jsonPath("$.sittingRecords[0].pm").isEmpty(),
                jsonPath("$.sittingRecords[0].createdDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserName").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserName").isNotEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidButNoEpimsAndHasMatchingRecords() throws Exception {

        SittingRecord sittingRecord = createSittingRecord(2L, "123", "BBA3",
                                                          HIGHCOURT, "4923421", "1",
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory1 = createStatusHistory(JASON_BOURNE, "11233",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord.addStatusHistory(statusHistory1);
        sittingRecord = recordRepository.save(sittingRecord);
        sittingRecord.getFirstStatusHistory();

        StatusHistory statusHistory2 = createStatusHistory(JACKIE_CHAN, "11255",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord.addStatusHistory(statusHistory2);
        assertEquals(statusHistory2.getStatusId(), sittingRecord.getStatusId());
        historyRepository.save(statusHistory2);
        sittingRecord = recordRepository.save(sittingRecord);

        StatusHistory statusHistory3 = createStatusHistory(DENZEL_WASHINGTON, "11266",
                                                           LocalDateTime.now(), StatusId.PUBLISHED.name());
        sittingRecord.addStatusHistory(statusHistory3);
        assertEquals(statusHistory3.getStatusId(), sittingRecord.getStatusId());
        statusHistory3 = historyRepository.save(statusHistory3);
        sittingRecord = recordRepository.save(sittingRecord);

        SittingRecord persistedSittingRecord = recordRepository.findAll().get(0);
        assertEquals(statusHistory3.getStatusId(), persistedSittingRecord.getStatusId());

        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertEquals(persistedSittingRecord.getStatusHistories().get(0), sittingRecord.getStatusHistories().get(0));
        assertEquals(persistedSittingRecord.getStatusHistories().get(1), sittingRecord.getStatusHistories().get(1));

        assertTrue(persistedSittingRecord.equalsDomainObject(sittingRecord));

        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_NO_EPIMMS_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "BBA3")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("1"),
                jsonPath(RECORDING_USERS_JSON_CONST).exists(),
                jsonPath(RECORDING_USERS_JSON_CONST).isArray(),
                jsonPath(RECORDING_USERS_JSON_CONST).isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingRecordId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingDate").isNotEmpty(),
                jsonPath("$.sittingRecords[0].statusId").value(StatusId.PUBLISHED.name()),
                jsonPath("$.sittingRecords[0].regionId").value("1"),
                jsonPath("$.sittingRecords[0].regionName").value(LONDON),
                jsonPath("$.sittingRecords[0].epimsId").value("123"),
                jsonPath("$.sittingRecords[0].hmctsServiceId").value("BBA3"),
                jsonPath("$.sittingRecords[0].personalCode").value("4923421"),
                jsonPath("$.sittingRecords[0].personalName").value(JOE_BLOGGS),
                jsonPath("$.sittingRecords[0].judgeRoleTypeId").value(HIGHCOURT),
                jsonPath("$.sittingRecords[0].am").value("AM"),
                jsonPath("$.sittingRecords[0].pm").isEmpty(),
                jsonPath("$.sittingRecords[0].createdDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserName").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserName").isNotEmpty()
            )
            .andReturn();
    }

    @Test
    void shouldHaveOkResponseWhenRequestIsValidButNoEpimsNoRegionAndHasMatchingRecords() throws Exception {

        SittingRecord sittingRecord = createSittingRecord(2L, "123", "BBA3",
                                                          HIGHCOURT, "4923421", "1",
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory1 = createStatusHistory(JASON_BOURNE, "11233",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord.addStatusHistory(statusHistory1);
        sittingRecord = recordRepository.save(sittingRecord);
        sittingRecord.getFirstStatusHistory();

        StatusHistory statusHistory2 = createStatusHistory(JACKIE_CHAN, "11255",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord.addStatusHistory(statusHistory2);
        assertEquals(statusHistory2.getStatusId(), sittingRecord.getStatusId());
        historyRepository.save(statusHistory2);
        sittingRecord = recordRepository.save(sittingRecord);

        StatusHistory statusHistory3 = createStatusHistory(DENZEL_WASHINGTON, "11266",
                                                           LocalDateTime.now(), StatusId.PUBLISHED.name());
        sittingRecord.addStatusHistory(statusHistory3);
        assertEquals(statusHistory3.getStatusId(), sittingRecord.getStatusId());
        statusHistory3 = historyRepository.save(statusHistory3);
        sittingRecord = recordRepository.save(sittingRecord);

        SittingRecord persistedSittingRecord = recordRepository.findAll().get(0);
        assertEquals(statusHistory3.getStatusId(), persistedSittingRecord.getStatusId());

        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertEquals(persistedSittingRecord.getStatusHistories().get(0), sittingRecord.getStatusHistories().get(0));
        assertEquals(persistedSittingRecord.getStatusHistories().get(1), sittingRecord.getStatusHistories().get(1));

        assertThat(persistedSittingRecord.equalsDomainObject(sittingRecord));

        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_NO_EPIMMS_NO_REGION_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "BBA3")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("1"),
                jsonPath(RECORDING_USERS_JSON_CONST).exists(),
                jsonPath(RECORDING_USERS_JSON_CONST).isArray(),
                jsonPath(RECORDING_USERS_JSON_CONST).isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingRecordId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].sittingDate").isNotEmpty(),
                jsonPath("$.sittingRecords[0].statusId").value(StatusId.PUBLISHED.name()),
                jsonPath("$.sittingRecords[0].regionId").value("1"),
                jsonPath("$.sittingRecords[0].regionName").value(LONDON),
                jsonPath("$.sittingRecords[0].epimsId").value("123"),
                jsonPath("$.sittingRecords[0].hmctsServiceId").value("BBA3"),
                jsonPath("$.sittingRecords[0].personalCode").value("4923421"),
                jsonPath("$.sittingRecords[0].personalName").value(JOE_BLOGGS),
                jsonPath("$.sittingRecords[0].judgeRoleTypeId").value(HIGHCOURT),
                jsonPath("$.sittingRecords[0].am").value("AM"),
                jsonPath("$.sittingRecords[0].pm").isEmpty(),
                jsonPath("$.sittingRecords[0].createdDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].createdByUserName").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedDateTime").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserId").isNotEmpty(),
                jsonPath("$.sittingRecords[0].changedByUserName").isNotEmpty()
            )
            .andReturn();
    }


    @Test
    void shouldHaveOkResponseWhenRequestIsValidButNoEpimsNoRegionAndHasMultipleMatchingRecords() throws Exception {

        SittingRecord sittingRecord1 = createSittingRecord(2L, "123", "BBA3",
                                                          HIGHCOURT, "4923421", "1",
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory1a = createStatusHistory(JASON_BOURNE, "11233",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord1.addStatusHistory(statusHistory1a);
        sittingRecord1 = recordRepository.save(sittingRecord1);
        sittingRecord1.getFirstStatusHistory();

        StatusHistory statusHistory1b = createStatusHistory(DENZEL_WASHINGTON, "11266",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord1.addStatusHistory(statusHistory1b);
        assertEquals(statusHistory1b.getStatusId(), sittingRecord1.getStatusId());
        historyRepository.save(statusHistory1b);
        recordRepository.save(sittingRecord1);

        SittingRecord sittingRecord2 = createSittingRecord(3L, "124", "BBA3",
                                                          HIGHCOURT, "4923422", "2",
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory2a = createStatusHistory(JASON_BOURNE, "11244",
                                                           LocalDateTime.now(), StatusId.RECORDED.name());
        sittingRecord2.addStatusHistory(statusHistory2a);
        sittingRecord2 = recordRepository.save(sittingRecord2);
        sittingRecord2.getFirstStatusHistory();

        StatusHistory statusHistory2b = createStatusHistory(JACKIE_CHAN, "11255",
                                                           LocalDateTime.now(), StatusId.SUBMITTED.name());
        sittingRecord2.addStatusHistory(statusHistory2b);
        assertEquals(statusHistory2b.getStatusId(), sittingRecord2.getStatusId());
        historyRepository.save(statusHistory2b);
        recordRepository.save(sittingRecord2);

        SittingRecord persistedSittingRecord1 = recordRepository.findAll().get(0);
        assertEquals(statusHistory1b.getStatusId(), persistedSittingRecord1.getStatusId());

        SittingRecord persistedSittingRecord2 = recordRepository.findAll().get(1);
        assertEquals(statusHistory2b.getStatusId(), persistedSittingRecord2.getStatusId());

        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_NO_EPIMMS_NO_REGION_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post(SEARCH_URL, "BBA3")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(updatedRecord))
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordCount").value("2")
            )
            .andReturn();
    }

    @Test
    void shouldReturn400ResponseWhenPathVariableHmctsServiceCodeNotSet() throws Exception {
        String requestJson = Resources.toString(getResource(SEARCH_SITTING_RECORDS_JSON), UTF_8);
        String updatedRecord = requestJson.replace(TO_DATE_CONST, LocalDate.now().toString());
        mockMvc
            .perform(post("/sitting-records/searchSittingRecords")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(updatedRecord))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].fieldName")
                           .value("PathVariable"))
            .andExpect(jsonPath("$.errors[0].message")
                           .value("hmctsServiceCode is mandatory"))
            .andReturn();
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecordsWithoutMandatoryFields.json"), UTF_8);
        MvcResult response = mockMvc
            .perform(post("/sitting-records/searchSittingRecords")
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
            .perform(post(SEARCH_URL, "2")
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

    private SittingRecord createSittingRecord(Long contractTypeId, String epimsId, String hmctsServiceId,
                                              String judgeRoleTypeId, String personalCode, String regionId,
                                              String statusId) {
        return SittingRecord.builder()
            .am(true)
            .contractTypeId(contractTypeId)
            .epimsId(epimsId)
            .hmctsServiceId(hmctsServiceId)
            .judgeRoleTypeId(judgeRoleTypeId)
            .personalCode(personalCode)
            .pm(false)
            .regionId(regionId)
            .sittingDate(LocalDate.now().minusDays(2L))
            .statusId(statusId)
            .build();
    }

    private StatusHistory createStatusHistory(String changedByName, String changedByUserId,
                                              LocalDateTime changedDateTime,
                                              String statusId) {
        return StatusHistory.builder()
            .changedByName(changedByName)
            .changedByUserId(changedByUserId)
            .changedDateTime(changedDateTime)
            .statusId(statusId)
            .build();
    }
}
