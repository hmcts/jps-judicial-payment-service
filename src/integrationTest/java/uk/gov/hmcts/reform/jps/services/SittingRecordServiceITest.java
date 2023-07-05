package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    public static final String EPIM_ID = "123";
    public static final String SSC_ID = "ssc_id";
    private SittingRecordService sittingRecordService;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_NAME = "John Doe";
    private static final String USER_ID_2 = UUID.randomUUID().toString();
    private static final String USER_NAME_2 = "Peter Parker";
    private static final String USER_ID_ALTERNATE = UUID.randomUUID().toString();
    private static final String USER_NAME_ALTERNATE = "Bruce Wayne";
    private static final String USER_NAME_FIXED = "Recorder";
    private static final String USER_ID_FIXED = "d139a314-eb40-45f4-9e7a-9e13f143cc3a";
    private static final String STATUS_ID_FIXED = "RECORDED";
    private static final String REGION_ID_FIXED = "1";
    private static final String EPIMS_ID_FIXED = "852649";
    private static final String PERSONAL_CODE_FIXED = "4918178";
    private static final String JUDGE_ROLE_TYPE_ID_FIXED = "Judge";

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
        assertTrue(actual.equalsDomainObject(sittingRecord));
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
        assertTrue(response.get(0).equalsDomainObject(sittingRecord));
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
            .extracting(SittingRecord_.CONTRACT_TYPE_ID)
            .containsExactlyInAnyOrder(
                11L, 12L, 13L, 14L, 15L
            );

        for (uk.gov.hmcts.reform.jps.model.out.SittingRecord sittingRecord : response) {
            assertThat(sittingRecord.getStatusHistories())
                .extracting(StatusHistory_.CHANGE_BY_USER_ID)
                .contains(USER_ID);
        }
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
        assertThat(response)
            .as("Extracting unique value by status")
            .extracting(SittingRecord_.CONTRACT_TYPE_ID, SittingRecord_.STATUS_ID)
            .contains(
                tuple(21L, STATUS_ID_FIXED),
                tuple(22L, STATUS_ID_FIXED)
            );

        for (uk.gov.hmcts.reform.jps.model.out.SittingRecord sittingRecord : response) {
            assertThat(sittingRecord.getStatusHistories())
                .as("Extracting change by user")
                .extracting(StatusHistory_.CHANGE_BY_USER_ID, StatusHistory_.STATUS_ID)
                .contains(
                    tuple(USER_ID, STATUS_ID_FIXED)
                );
        }
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

        assertThat(totalRecordCount).isEqualTo(25);
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
            .extracting(SittingRecord_.SITTING_DATE, SittingRecord_.REGION_ID, SittingRecord_.EPIMS_ID,
                        SittingRecord_.PERSONAL_CODE, SittingRecord_.JUDGE_ROLE_TYPE_ID,
                        SittingRecord_.CONTRACT_TYPE_ID, SittingRecord_.AM, SittingRecord_.PM,
                        SittingRecord_.STATUS_ID, SittingRecord_.HMCTS_SERVICE_ID
            )
            .contains(
                tuple(of(2023, MAY, 11), REGION_ID_FIXED, EPIMS_ID_FIXED, PERSONAL_CODE_FIXED,
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, false, true, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, APRIL, 10), REGION_ID_FIXED, EPIMS_ID_FIXED, PERSONAL_CODE_FIXED,
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, false, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, MARCH, 9), REGION_ID_FIXED, EPIMS_ID_FIXED, PERSONAL_CODE_FIXED,
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, true, STATUS_ID_FIXED, SSC_ID)
            );

        List<StatusHistory> statusHistories = statusHistoryRepository.findAll();
        assertThat(statusHistories)
            .extracting(StatusHistory_.STATUS_ID, StatusHistory_.CHANGE_BY_USER_ID, StatusHistory_.CHANGE_BY_NAME)
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
        assertThat(sittingRecord.getId()).isNotNull();

        StatusHistory statusHistorySubmitted1 = createStatusHistory("SUBMITTED", USER_ID_ALTERNATE,
                                                                    USER_NAME_ALTERNATE);
        sittingRecord.addStatusHistory(statusHistorySubmitted1);
        statusHistoryRepository.save(
            sittingRecord.getStatusHistories().get(sittingRecord.getStatusHistories().size() - 1));
        assertThat(sittingRecord.getId()).isNotNull();
        assertEquals(sittingRecord.getStatusHistories().size(), 2);

        StatusHistory statusHistoryDeleted1 = createStatusHistory("DELETED", USER_ID_ALTERNATE,
                                                                 USER_NAME_ALTERNATE);
        sittingRecord.addStatusHistory(statusHistoryDeleted1);
        statusHistoryRepository.save(
            sittingRecord.getStatusHistories().get(sittingRecord.getStatusHistories().size() - 1));
        assertThat(sittingRecord.getId()).isNotNull();
        assertEquals(sittingRecord.getStatusHistories().size(), 3);

        SittingRecord sittingRecord2 = createAndSaveSittingRecord(STATUS_ID_FIXED,2L, USER_ID_2, USER_NAME_2);
        assertThat(sittingRecord2).isNotNull();

        SittingRecord sittingRecord3 = createAndSaveSittingRecord(STATUS_ID_FIXED, 3L, USER_ID_ALTERNATE,
                                                           USER_NAME_ALTERNATE);

        StatusHistory statusHistorySubmitted3 = createStatusHistory("SUBMITTED", USER_ID, USER_NAME);
        sittingRecord3.addStatusHistory(statusHistorySubmitted3);

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

        assertEquals(USER_ID, actual.getCreatedByUserId());
        assertThat(response).hasSize(1);

        StatusHistory statusHistoryCreated1 = sittingRecord.getStatusHistories().get(0);
        assertTrue(actual.equalsDomainObject(sittingRecord));
        assertTrue(statusHistoryCreated1.equals(actual.getStatusHistories().get(0)));

    }

    private SittingRecord createAndSaveSittingRecord(String statusId, Long counter, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(statusId,  counter, userId, userName);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        assertThat(sittingRecord.getStatusHistories()).isNotNull();
        assertFalse(sittingRecord.getStatusHistories().isEmpty());
        return persistedSittingRecord;
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
        StatusHistory statusHistory = createStatusHistory(sittingRecord.getStatusId(), userId, userName);
        sittingRecord.addStatusHistory(statusHistory);
        return sittingRecord;
    }

    private StatusHistory createStatusHistory(String statusId, String userId, String userName) {
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
