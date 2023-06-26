package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.DateOrder.ASCENDING;
import static uk.gov.hmcts.reform.jps.model.DateOrder.DESCENDING;

class SittingRecordServiceITest extends BaseTest {

    @Autowired
    private SittingRecordRepository sittingRecordRepository;
    @Autowired
    private StatusHistoryRepository statusHistoryRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordServiceITest.class);

    public static final String EPIM_ID = "123";
    public static final String SSC_ID = "ssc_id";
    public static final String CONTRACT_TYPE_ID = "contractTypeId";
    public static final String CREATED_BY_USER_ID = "createdByUserId";

    private SittingRecordService sittingRecordService;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_NAME = "John Doe";
    private static final String USER_ID_ALTERNATE = UUID.randomUUID().toString();
    private static final String USER_NAME_ALTERNATE = "Bruce Wayne";
    private static final String USER_NAME_FIXED = "Recorder";
    private static final String USER_ID_FIXED = "d139a314-eb40-45f4-9e7a-9e13f143cc3a";
    private static final String STATUS_ID_FIXED = "RECORDED";

    @BeforeEach
    void beforeEach() {
        sittingRecordRepository.deleteAll();
        sittingRecordService = new SittingRecordService(sittingRecordRepository);
    }

    @Test
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = createAndSaveSittingRecord(STATUS_ID_FIXED,2L, USER_ID, USER_NAME);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimsId(sittingRecord.getEpimsId())
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId()
        );


        uk.gov.hmcts.reform.jps.model.out.SittingRecord actual = response.get(0);

        assertThat(response).hasSize(1);
        LOGGER.debug("actual:{}", actual);

        assertSittingRecordEqualsExpected(sittingRecord, actual);
    }

    @Test
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {

        SittingRecord sittingRecord = createAndSaveSittingRecord(STATUS_ID_FIXED, 2L, USER_ID, USER_NAME);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimsId(sittingRecord.getEpimsId())
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .personalCode("001")
            .judgeRoleTypeId("HighCourt")
            .createdByUserId(USER_ID)
            .statusId(StatusId.RECORDED)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId()
        );

        assertThat(response).hasSize(1);

        LOGGER.info("response.get(0):{}", response.get(0));

        assertSittingRecordEqualsExpected(sittingRecord, response.get(0));
    }

    @Test
    void shouldReturnOffset10RecordsOnwardsInAscendingOrder() {
        int recordCount = 25;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            SSC_ID
        );

        assertThat(response).hasSize(5);

        assertThat(response)
            .extracting(CONTRACT_TYPE_ID, CREATED_BY_USER_ID)
            .contains(
                tuple(11L, USER_ID),
                tuple(12L, USER_ID),
                tuple(13L, USER_ID),
                tuple(14L, USER_ID),
                tuple(15L, USER_ID)
            );
    }


    @Test
    void shouldReturnLast2RecordsWhenSortOrderIsDescending() {
        int recordCount = 22;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(DESCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            SSC_ID
        );

        assertThat(response).hasSize(2);

        List<StatusHistory> responseStatusHistory = new ArrayList<>();
        responseStatusHistory.add(response.get(0).getStatusHistories().get(0));
        responseStatusHistory.add(response.get(1).getStatusHistories().get(0));

        assertThat(responseStatusHistory)
            .as("Extracting unique value by user")
            .extracting(CONTRACT_TYPE_ID, CREATED_BY_USER_ID)
            .contains(
                tuple(21L, USER_ID),
                tuple(22L, USER_ID)
            );
    }

    @Test
    void shouldReturnTotalRecordCounts() {
        int recordCount = 25;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        int totalRecordCount = sittingRecordService.getTotalRecordCount(
            recordSearchRequest,
            SSC_ID
        );

        assertThat(totalRecordCount)
            .isEqualTo(25);
    }

    @Test
    void shouldRecordSittingRecordsWhenAllDataIsPresent() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        recordSittingRecordRequest.getRecordedSittingRecords()
            .forEach(sittingRecordRequest -> sittingRecordRequest.setRegionId("1"));

        sittingRecordService.saveSittingRecords(SSC_ID, recordSittingRecordRequest);
        List<SittingRecord> savedSittingRecords = sittingRecordRepository.findAll();

        assertThat(savedSittingRecords)
            .extracting("sittingDate", "regionId", "epimsId", "personalCode", "judgeRoleTypeId", CONTRACT_TYPE_ID,
                        "am", "pm", "statusId", "hmctsServiceId"
            )
            .contains(
                tuple(of(2023, MAY, 11), "1", "852649", "4918178", "Judge", 1L, false, true, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, APRIL, 10), "1", "852649", "4918178", "Judge", 1L, true, false, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, MARCH, 9), "1", "852649", "4918178", "Judge", 1L, true, true, STATUS_ID_FIXED, SSC_ID)
            );

        List<StatusHistory> statusHistories = statusHistoryRepository.findAll();
        assertThat(statusHistories)
            .extracting("statusId", "changeByUserId", "changeByName")
            .contains(
                tuple(STATUS_ID_FIXED, USER_ID_FIXED, USER_NAME_FIXED),
                tuple(STATUS_ID_FIXED, USER_ID_FIXED, USER_NAME_FIXED),
                tuple(STATUS_ID_FIXED, USER_ID_FIXED, USER_NAME_FIXED)
            );

        assertThat(statusHistories).describedAs("Created date assertion")
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangeDateTime()));
    }

    @Test
    void shouldReturnQueriedRecordsCreatedByGivenUser() {
        SittingRecord sittingRecord = createAndSaveSittingRecord(STATUS_ID_FIXED,2L, USER_ID, USER_NAME);
        assertThat(sittingRecord).isNotNull();

        StatusHistory statusHistoryCreated1 = sittingRecord.getStatusHistories().get(0);
        LOGGER.info("statusHistoryCreated:{}", statusHistoryCreated1);

        StatusHistory statusHistorySubmitted1 = createAndSaveStatusHistory("SUBMITTED", USER_ID_ALTERNATE,
                                                                 USER_NAME_ALTERNATE, sittingRecord);
        LOGGER.info("statusHistorySubmitted1:{}", statusHistorySubmitted1);

        StatusHistory statusHistoryDeleted1 = createAndSaveStatusHistory("DELETED", USER_ID_ALTERNATE,
                                                                 USER_NAME_ALTERNATE, sittingRecord);
        LOGGER.info("statusHistoryDeleted1:{}", statusHistoryDeleted1);

        SittingRecord sittingRecord3 = createAndSaveSittingRecord(STATUS_ID_FIXED, 3L, USER_ID_ALTERNATE,
                                                           USER_NAME_ALTERNATE);

        StatusHistory statusHistoryCreated2 = sittingRecord3.getStatusHistories().get(0);
        LOGGER.info("statusHistoryCreated2:{}", statusHistoryCreated2);

        StatusHistory statusHistorySubmitted2 = createAndSaveStatusHistory("SUBMITTED", USER_ID,
                                                   USER_NAME, sittingRecord3);
        LOGGER.info("statusHistorySubmitted2:{}", statusHistorySubmitted2);

        int recordCount = 22;
        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimsId(sittingRecord.getEpimsId())
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .createdByUserId(USER_ID)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId()
        );

        uk.gov.hmcts.reform.jps.model.out.SittingRecord actual = response.get(0);

        assertEquals(USER_ID, response.get(0).getCreatedByUserId());
        assertEquals(USER_ID, response.get(1).getCreatedByUserId());
        assertThat(response).hasSize(1);

        LOGGER.debug("expected:{}", sittingRecord);
        LOGGER.debug("actual:{}", actual);


        assertSittingRecordEqualsExpected(sittingRecord, actual);
        assertStatusHistoriesEqualsExpected(statusHistoryCreated1, actual.getStatusHistories().get(0));

    }

    private void assertSittingRecordEqualsExpected(SittingRecord expected,
                                                   uk.gov.hmcts.reform.jps.model.out.SittingRecord actual) {
        assertThat(expected.getContractTypeId()).isEqualTo(actual.getContractTypeId());
        assertThat(expected.getEpimsId()).isEqualTo(actual.getEpimsId());
        assertThat(expected.getHmctsServiceId()).isEqualTo(actual.getHmctsServiceId());
        assertThat(expected.getJudgeRoleTypeId()).isEqualTo(actual.getJudgeRoleTypeId());
        assertThat(expected.getPersonalCode()).isEqualTo(actual.getPersonalCode());
        assertThat(expected.getRegionId()).isEqualTo(actual.getRegionId());
        assertThat(expected.getSittingDate()).isEqualTo(actual.getSittingDate());
        assertEquals(expected.getId(), actual.getSittingRecordId());
        assertThat(expected.getStatusId()).isEqualTo(actual.getStatusId());
    }

    private void assertStatusHistoriesEqualsExpected(StatusHistory expected, StatusHistory actual) {
        assertEquals(expected.getId(), actual.getId());
    }

    private SittingRecord createAndSaveSittingRecord(String statusId, Long counter, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(statusId,  counter, userId, userName);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        sittingRecord.setId(persistedSittingRecord.getId());
        statusHistoryRepository.saveAll(sittingRecord.getStatusHistories());
        assertThat(sittingRecord.getStatusHistories()).isNotNull();
        assertFalse(sittingRecord.getStatusHistories().isEmpty());
        return sittingRecord;
    }

    private SittingRecord createSittingRecord(String statusId, long counter, String userId, String userName) {
        SittingRecord.SittingRecordBuilder builder = SittingRecord.builder();
        SittingRecord sittingRecord = builder
            .sittingDate(LocalDate.now().minusDays(counter))
            .statusId(statusId)
            .regionId("1")
            .epimsId(EPIM_ID)
            .hmctsServiceId(SSC_ID)
            .personalCode("001")
            .contractTypeId(counter)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .build();
        StatusHistory statusHistory = createStatusHistoryRecord(sittingRecord.getStatusId(), userId, userName);
        statusHistory.setSittingRecord(sittingRecord);
        sittingRecord.addStatusHistory(statusHistory);
        return sittingRecord;
    }

    private StatusHistory createAndSaveStatusHistory(String statusId, String userId, String userName,
                                                     SittingRecord sittingRecord) {

        StatusHistory statusHistory = createStatusHistoryRecord(statusId, userId, userName);
        statusHistory.setSittingRecord(sittingRecord);
        sittingRecord.addStatusHistory(statusHistory);
        StatusHistory persistedStatusHistory = statusHistoryRepository.save(statusHistory);

        sittingRecord.setStatusId(statusHistory.getStatusId());
        statusHistory.setId(persistedStatusHistory.getId());
        return statusHistory;
    }

    private StatusHistory createStatusHistoryRecord(String statusId, String userId, String userName) {
        return StatusHistory.builder()
            .statusId(statusId)
            .changeDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
            .changeByUserId(userId)
            .changeByName(userName)
            .build();
    }

    private void createMultipleRecords(int count) {
        for (long i = count; i > 0; i--) {
            createAndSaveSittingRecord(STATUS_ID_FIXED, i, USER_ID, USER_NAME);
        }
    }

}
