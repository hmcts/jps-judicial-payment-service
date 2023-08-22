package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
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
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

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


@Transactional
class SittingRecordServiceITest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordServiceITest.class);

    @Autowired
    private SittingRecordRepository sittingRecordRepository;
    @Autowired
    private SittingRecordService sittingRecordService;
    @Autowired
    private StatusHistoryService statusHistoryService;
    @Autowired
    private ObjectMapper objectMapper;

    public static final String EPIMMS_ID = "852649";
    public static final String HMCTS_SERVICE_CODE = "BBA3";

    @Autowired
    private StatusHistoryService statusHistoryService;
    @Autowired
    private SittingRecordService sittingRecordService;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_NAME = "John Doe";
    private static final String USER_NAME_FIXED = "Recorder";
    private static final String USER_ID_FIXED = "d139a314-eb40-45f4-9e7a-9e13f143cc3a";
    private static final String REGION_ID_FIXED = "1";
    private static final String EPIMMS_ID_FIXED = "852649";
    private static final String JUDGE_ROLE_TYPE_ID_FIXED = "Judge";

    @BeforeEach
    void beforeEach() {
        statusHistoryRepository.deleteAll();
        sittingRecordRepository.deleteAll();
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = createAndSaveSittingRecord(StatusId.RECORDED.name(), 2L, USER_ID, USER_NAME);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimmsId(sittingRecord.getEpimmsId())
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
        LOGGER.debug("actual:        {}", actual);
        LOGGER.debug("sittingRecord: {}", sittingRecord);
        assertTrue(actual.equalsDomainObject(sittingRecord));
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {

        SittingRecord sittingRecord = createAndSaveSittingRecord(StatusId.RECORDED.name(), 2L, USER_ID, USER_NAME);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimmsId(sittingRecord.getEpimmsId())
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
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnOffset10RecordsOnwardsInAscendingOrder() {
        int recordCount = 25;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimmsId(EPIMMS_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            HMCTS_SERVICE_CODE
        );

        assertThat(response).hasSize(5);

        assertThat(response)
            .extracting(SittingRecord_.CONTRACT_TYPE_ID)
            .containsExactlyInAnyOrder(
                11L, 12L, 13L, 14L, 15L
            );

        for (uk.gov.hmcts.reform.jps.model.out.SittingRecord sittingRecord : response) {
            assertThat(sittingRecord.getStatusHistories())
                .extracting(StatusHistory_.CHANGED_BY_USER_ID)
                .contains(USER_ID);
        }
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnLast2RecordsWhenSortOrderIsDescending() {
        int recordCount = 22;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimmsId(EPIMMS_ID)
            .dateOrder(DESCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            HMCTS_SERVICE_CODE
        );

        assertThat(response).hasSize(2);
        assertThat(response)
            .as("Extracting unique value by status")
            .extracting(SittingRecord_.CONTRACT_TYPE_ID, SittingRecord_.STATUS_ID)
            .contains(
                tuple(21L, StatusId.RECORDED.name()),
                tuple(22L, StatusId.RECORDED.name())
            );

        for (uk.gov.hmcts.reform.jps.model.out.SittingRecord sittingRecord : response) {
            assertThat(sittingRecord.getStatusHistories())
                .as("Extracting change by user")
                .extracting(StatusHistory_.CHANGED_BY_USER_ID, StatusHistory_.STATUS_ID)
                .contains(
                    tuple(USER_ID, StatusId.RECORDED.name())
                );
        }
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnTotalRecordCounts() {
        int recordCount = 25;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimmsId(EPIMMS_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .build();

        long totalRecordCount = sittingRecordService.getTotalRecordCount(
            recordSearchRequest,
            HMCTS_SERVICE_CODE
        );

        assertThat(totalRecordCount).isEqualTo(25);
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldRecordSittingRecordsWhenAllDataIsPresent() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        recordSittingRecordRequest.getRecordedSittingRecords()
            .forEach(sittingRecordRequest -> sittingRecordRequest.setRegionId("1"));

        sittingRecordService.saveSittingRecords(HMCTS_SERVICE_CODE, recordSittingRecordRequest);
        List<SittingRecord> savedSittingRecords = sittingRecordRepository.findAll();

        assertThat(savedSittingRecords)
            .extracting(SittingRecord_.SITTING_DATE, SittingRecord_.REGION_ID, SittingRecord_.EPIMMS_ID,
                        SittingRecord_.PERSONAL_CODE, SittingRecord_.JUDGE_ROLE_TYPE_ID,
                        SittingRecord_.CONTRACT_TYPE_ID, SittingRecord_.AM, SittingRecord_.PM,
                        SittingRecord_.STATUS_ID, SittingRecord_.HMCTS_SERVICE_ID
            )
            .contains(
                tuple(of(2023, MAY, 11), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918178",
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, false, true, StatusId.RECORDED.name(), HMCTS_SERVICE_CODE),
                tuple(of(2023, APRIL, 10), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918179",
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, false, StatusId.RECORDED.name(), HMCTS_SERVICE_CODE),
                tuple(of(2023, MARCH, 9), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918180",
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, true, StatusId.RECORDED.name(), HMCTS_SERVICE_CODE)
            );

        List<StatusHistory> statusHistories = statusHistoryService.findAll();
        assertThat(statusHistories)
            .extracting(StatusHistory_.STATUS_ID, StatusHistory_.CHANGED_BY_USER_ID, StatusHistory_.CHANGED_BY_NAME)
            .contains(
                tuple(StatusId.RECORDED.name(), USER_ID_FIXED, USER_NAME_FIXED),
                tuple(StatusId.RECORDED.name(), USER_ID_FIXED, USER_NAME_FIXED),
                tuple(StatusId.RECORDED.name(), USER_ID_FIXED, USER_NAME_FIXED)
            );

        assertThat(statusHistories).describedAs("Created date assertion")
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangedDateTime()));
    }

    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void shouldReturnQueriedRecordsCreatedByGivenUser() {
        final String Bruce_Wayne = "Bruce Wayne";
        final String Clark_Kent = "Clark Kent";
        final String Peter_Parker = "Peter Parker";
        final String Bruce_Wayne_ID = "bruce-100011";
        final String Clark_Kent_ID = "clark-100022";
        final String Peter_Parker_ID = "peter-10033";

        SittingRecord sittingRecord = createAndSaveSittingRecord(StatusId.RECORDED.name(),2L, Bruce_Wayne_ID,
                                                                 Bruce_Wayne);

        StatusHistory statusHistorySubmitted1 = createStatusHistory("SUBMITTED", Clark_Kent_ID, Clark_Kent);
        statusHistoryService.saveStatusHistory(statusHistorySubmitted1, sittingRecord);
        assertThat(sittingRecord.getId()).isNotNull();
        assertEquals(sittingRecord.getStatusHistories().size(), 2);

        StatusHistory statusHistoryDeleted1 = createStatusHistory("DELETED", Peter_Parker_ID, Peter_Parker);
        statusHistoryService.saveStatusHistory(statusHistoryDeleted1, sittingRecord);
        assertThat(sittingRecord.getId()).isNotNull();
        assertEquals(sittingRecord.getStatusHistories().size(), 3);

        createAndSaveSittingRecord(StatusId.RECORDED.name(), 2L, Peter_Parker_ID, Peter_Parker);

        SittingRecord sittingRecord3 = createAndSaveSittingRecord(StatusId.RECORDED.name(), 1L, Clark_Kent_ID,
                                                                  Clark_Kent);
        StatusHistory statusHistorySubmitted3 = createStatusHistory(StatusId.SUBMITTED.name(),
                                                                    Bruce_Wayne_ID, Bruce_Wayne);
        statusHistoryService.saveStatusHistory(statusHistorySubmitted3, sittingRecord3);

        int recordCount = 22;
        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimmsId(sittingRecord.getEpimmsId())
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(recordCount))
            .dateRangeTo(LocalDate.now())
            .createdByUserId(Bruce_Wayne_ID)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId()
        );

        uk.gov.hmcts.reform.jps.model.out.SittingRecord actual = response.get(0);

        LOGGER.debug("changedByUserId:{}", actual.getChangedByUserId());
        LOGGER.debug("actual:{}", actual);
        LOGGER.debug("actual.statusHistories:{}", actual.getStatusHistories());

        assertEquals(Bruce_Wayne_ID, actual.getCreatedByUserId());
        assertThat(response).hasSize(1);

        assertTrue(actual.equalsDomainObject(sittingRecord));
        StatusHistory statusHistoryCreated1 = sittingRecord.getStatusHistories().get(0);
        LOGGER.debug("statusHistoryCreated1:{}", statusHistoryCreated1);
        LOGGER.debug("actual               :{}", actual.getFirstStatusHistory());
        assertTrue(statusHistoryCreated1.equals(actual.getFirstStatusHistory()));

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
            .epimmsId(EPIMMS_ID)
            .hmctsServiceId(HMCTS_SERVICE_CODE)
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
            .changedDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
            .changedByUserId(userId)
            .changedByName(userName)
            .build();
    }

    private void createMultipleRecords(int count) {
        for (long i = count; i > 0; i--) {
            createAndSaveSittingRecord(StatusId.RECORDED.name(), i, USER_ID, USER_NAME);
        }
    }

}
