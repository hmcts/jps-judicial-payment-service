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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.DateOrder.ASCENDING;
import static uk.gov.hmcts.reform.jps.model.DateOrder.DESCENDING;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

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
    private static final String USER_ID_FIXED =  "d139a314-eb40-45f4-9e7a-9e13f143cc3a";
    private static final String STATUS_ID_FIXED =  "RECORDED";

    @BeforeEach
    void beforeEach() {
        sittingRecordRepository.deleteAll();
        sittingRecordService = new SittingRecordService(sittingRecordRepository);
    }

    @Test
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = createSittingRecord(2);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        statusHistoryRepository.saveAll(sittingRecord.getStatusHistories());

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


        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = createSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);
        LOGGER.info("expected:{}", expected);
        LOGGER.info("response.get(0):{}", response.get(0));

        assertThat(expected).isEqualTo(response.get(0));
    }

    @Test
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {
        SittingRecord sittingRecord = createSittingRecord(2);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        statusHistoryRepository.saveAll(sittingRecord.getStatusHistories());

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

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = createSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);

        LOGGER.info("expected:{}", expected);
        LOGGER.info("response.get(0):{}", response.get(0));

        assertSittingRecordEqualsExpected(expected, response.get(0));
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
        assertThat(response)
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
            .extracting("sittingDate","regionId",  "epimsId", "personalCode", "judgeRoleTypeId", CONTRACT_TYPE_ID,
                        "am", "pm", "statusId", "hmctsServiceId")
            .contains(
                tuple(of(2023, MAY, 11), "1", "852649", "4918178", "Judge", 1L, false, true, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, APRIL,10), "1", "852649", "4918178", "Judge", 1L, true, false, STATUS_ID_FIXED, SSC_ID),
                tuple(of(2023, MARCH,9), "1", "852649", "4918178", "Judge", 1L, true, true, STATUS_ID_FIXED, SSC_ID)
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
        SittingRecord sittingRecord = createSittingRecord(2);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

        StatusHistory statusHistory = createStatusHistoryRecord("SUBMITTED", USER_ID_ALTERNATE, USER_NAME_ALTERNATE);
        statusHistory.setSittingRecord(sittingRecord);
        sittingRecord.addStatusHistory(statusHistory);
        StatusHistory persistedStatusHistory = statusHistoryRepository.save(statusHistory);

        sittingRecord.setStatusId(statusHistory.getStatusId());
        sittingRecord.setId(persistedSittingRecord.getId());
        persistedSittingRecord = sittingRecordRepository.save(sittingRecord);


        statusHistory = createStatusHistoryRecord("DELETED", USER_ID_ALTERNATE, USER_NAME_ALTERNATE);
        statusHistory.setSittingRecord(sittingRecord);
        sittingRecord.addStatusHistory(statusHistory);
        persistedStatusHistory = statusHistoryRepository.save(statusHistory);

        sittingRecord.setStatusId(statusHistory.getStatusId());
        sittingRecord.setId(persistedSittingRecord.getId());
        persistedSittingRecord = sittingRecordRepository.save(sittingRecord);

        SittingRecord sittingRecord3 = createSittingRecord(3, USER_ID_ALTERNATE, USER_NAME_ALTERNATE);
        SittingRecord persistedSittingRecord3 = sittingRecordRepository.save(sittingRecord3);
        assertThat(persistedSittingRecord3).isNotNull();

        statusHistory = createStatusHistoryRecord("SUBMITTED", USER_ID, USER_NAME);
        statusHistory.setSittingRecord(sittingRecord3);
        sittingRecord.addStatusHistory(statusHistory);
        StatusHistory persistedStatusHistory3 = statusHistoryRepository.save(statusHistory);


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

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = createSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);

        LOGGER.info("expected:{}", expected);
        LOGGER.info("response.get(0):{}", response.get(0));

        assertSittingRecordEqualsExpected(expected, response.get(0));
    }

    private void assertSittingRecordEqualsExpected(uk.gov.hmcts.reform.jps.model.out.SittingRecord expected,
                                                      uk.gov.hmcts.reform.jps.model.out.SittingRecord actual) {
        assertThat(expected.getAm()).isEqualTo(actual.getAm());
        assertThat(expected.getContractTypeId()).isEqualTo(actual.getContractTypeId());
        assertThat(expected.getEpimsId()).isEqualTo(actual.getEpimsId());
        assertThat(expected.getHmctsServiceId()).isEqualTo(actual.getHmctsServiceId());
        assertThat(expected.getJudgeRoleTypeId()).isEqualTo(actual.getJudgeRoleTypeId());
        assertThat(expected.getPersonalCode()).isEqualTo(actual.getPersonalCode());
        assertThat(expected.getPersonalName()).isEqualTo(actual.getPersonalName());
        assertThat(expected.getPm()).isEqualTo(actual.getPm());
        assertThat(expected.getRegionId()).isEqualTo(actual.getRegionId());
        assertThat(expected.getRegionName()).isEqualTo(actual.getRegionName());
        assertThat(expected.getSittingDate()).isEqualTo(actual.getSittingDate());
        assertThat(expected.getSittingRecordId()).isEqualTo(actual.getSittingRecordId());
        assertStatusHistoriesEqualsExpected(expected.getStatusHistories(), actual.getStatusHistories());
        assertThat(expected.getStatusId()).isEqualTo(actual.getStatusId());
    }

    private void assertStatusHistoriesEqualsExpected(List<StatusHistory> expected, List<StatusHistory> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getId(), actual.get(0).getId());
    }


    private uk.gov.hmcts.reform.jps.model.out.SittingRecord createSittingRecord(SittingRecord sittingRecord) {
        String notSet = null;
        return Optional.ofNullable(sittingRecord)
            .map(persistedSittingRecord -> uk.gov.hmcts.reform.jps.model.out.SittingRecord.builder()
                .sittingRecordId(persistedSittingRecord.getId())
                .sittingDate(persistedSittingRecord.getSittingDate())
                .statusId(persistedSittingRecord.getStatusId())
                .regionId(persistedSittingRecord.getRegionId())
                .epimsId(persistedSittingRecord.getEpimsId())
                .hmctsServiceId(persistedSittingRecord.getHmctsServiceId())
                .personalCode(persistedSittingRecord.getPersonalCode())
                .contractTypeId(persistedSittingRecord.getContractTypeId())
                .judgeRoleTypeId(persistedSittingRecord.getJudgeRoleTypeId())
                .am(persistedSittingRecord.isAm() ? AM.name() : notSet)
                .pm(persistedSittingRecord.isPm() ? PM.name() : notSet)
                .statusHistories(persistedSittingRecord.getStatusHistories())
                .build())
            .orElseThrow();
    }

    private SittingRecord createSittingRecord(long counter) {
        return createSittingRecord(counter, USER_ID, USER_NAME);
    }

    private SittingRecord createSittingRecord(long counter, String userId, String userName) {
        StatusId recorded = StatusId.RECORDED;
        SittingRecord.SittingRecordBuilder builder = SittingRecord.builder();
        SittingRecord sittingRecord = builder
            .sittingDate(LocalDate.now().minusDays(counter))
            .statusId(recorded.name())
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
            SittingRecord sittingRecord = createSittingRecord(i);
            SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
            statusHistoryRepository.saveAll(sittingRecord.getStatusHistories());
            assertThat(persistedSittingRecord).isNotNull();
        }
    }

}
