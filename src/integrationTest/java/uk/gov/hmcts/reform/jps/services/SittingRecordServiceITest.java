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
import uk.gov.hmcts.reform.jps.domain.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.DateOrder.ASCENDING;
import static uk.gov.hmcts.reform.jps.model.DateOrder.DESCENDING;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_LOCATION;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.CLOSED;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.PUBLISHED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;


@Transactional
class SittingRecordServiceITest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordServiceITest.class);

    private final SittingRecordRepository sittingRecordRepository;
    private final SittingRecordService sittingRecordService;
    private final StatusHistoryService statusHistoryService;
    private final ServiceService serviceService;
    private final ObjectMapper objectMapper;

    public static final String EPIMMS_ID = "852649";
    public static final String HMCTS_SERVICE_CODE = "BBA3";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_NAME = "John Doe";
    private static final String USER_NAME_FIXED = "Recorder";
    private static final String USER_ID_FIXED = "d139a314-eb40-45f4-9e7a-9e13f143cc3a";
    private static final String REGION_ID_FIXED = "1";
    private static final String EPIMMS_ID_FIXED = "852649";
    private static final String JUDGE_ROLE_TYPE_ID_FIXED = "Judge";
    private static final String JSON_RECORD_SITTING_RECORDS = "recordSittingRecords.json";

    @Autowired
    public SittingRecordServiceITest(SittingRecordRepository sittingRecordRepository,
                                     SittingRecordService sittingRecordService,
                                     StatusHistoryService statusHistoryService,
                                     ServiceService serviceService,
                                     ObjectMapper objectMapper) {

        this.sittingRecordRepository = sittingRecordRepository;
        this.sittingRecordService = sittingRecordService;
        this.statusHistoryService = statusHistoryService;
        this.serviceService = serviceService;
        this.objectMapper = objectMapper;
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = createAndSaveSittingRecord(RECORDED,2L, USER_ID, USER_NAME);

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
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {

        SittingRecord sittingRecord = createAndSaveSittingRecord(RECORDED, 2L, USER_ID, USER_NAME);

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
            .statusId(RECORDED)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId()
        );

        assertThat(response).hasSize(1);
        assertTrue(response.get(0).equalsDomainObject(sittingRecord));
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
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
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
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
            .containsExactly(
                tuple(21L, RECORDED),
                tuple(22L, RECORDED)
            );

        for (uk.gov.hmcts.reform.jps.model.out.SittingRecord sittingRecord : response) {
            assertThat(sittingRecord.getStatusHistories())
                .as("Extracting change by user")
                .extracting(StatusHistory_.CHANGED_BY_USER_ID, StatusHistory_.STATUS_ID)
                .contains(
                    tuple(USER_ID, RECORDED)
                );
        }
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
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
    @Sql(scripts = {RESET_DATABASE})
    void shouldRecordSittingRecordsWhenAllDataIsPresent() throws IOException {
        String requestJson = Resources.toString(getResource(JSON_RECORD_SITTING_RECORDS), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers
            .forEach(sittingRecordWrapper -> sittingRecordWrapper.setRegionId("1"));

        sittingRecordService.saveSittingRecords(HMCTS_SERVICE_CODE,
                                                sittingRecordWrappers,
                                                recordSittingRecordRequest.getRecordedByName(),
                                                recordSittingRecordRequest.getRecordedByIdamId());

        List<SittingRecord> savedSittingRecords = sittingRecordRepository.findAll();

        assertThat(savedSittingRecords)
            .extracting(SittingRecord_.SITTING_DATE, SittingRecord_.REGION_ID, SittingRecord_.EPIMMS_ID,
                        SittingRecord_.PERSONAL_CODE, SittingRecord_.JUDGE_ROLE_TYPE_ID,
                        SittingRecord_.CONTRACT_TYPE_ID, SittingRecord_.AM, SittingRecord_.PM,
                        SittingRecord_.STATUS_ID, SittingRecord_.HMCTS_SERVICE_ID
            )
            .containsExactly(
                tuple(of(2022, MAY, 11), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918500",
                      "Tester", 1L, false, true, RECORDED, HMCTS_SERVICE_CODE),
                tuple(of(2023, APRIL, 10), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918179",
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, false, RECORDED, HMCTS_SERVICE_CODE),
                tuple(of(2023, MARCH, 9), REGION_ID_FIXED, EPIMMS_ID_FIXED, "4918180",
                      JUDGE_ROLE_TYPE_ID_FIXED, 1L, true, true, RECORDED, HMCTS_SERVICE_CODE)
            );

        List<StatusHistory> statusHistories = statusHistoryService.findAll();
        assertThat(statusHistories)
            .extracting(StatusHistory_.STATUS_ID, StatusHistory_.CHANGED_BY_USER_ID, StatusHistory_.CHANGED_BY_NAME)
            .containsExactly(
                tuple(RECORDED, USER_ID_FIXED, USER_NAME_FIXED),
                tuple(RECORDED, USER_ID_FIXED, USER_NAME_FIXED),
                tuple(RECORDED, USER_ID_FIXED, USER_NAME_FIXED)
            );

        assertThat(statusHistories).describedAs("Created date assertion")
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangedDateTime()));
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldReturnQueriedRecordsCreatedByGivenUser() {
        final String Bruce_Wayne = "Bruce Wayne";
        final String Clark_Kent = "Clark Kent";
        final String Peter_Parker = "Peter Parker";
        final String Bruce_Wayne_ID = "bruce-100011";
        final String Clark_Kent_ID = "clark-100022";
        final String Peter_Parker_ID = "peter-10033";

        int recordCount = 22;
        LocalDate serviceOnboardedDate = LocalDate.now().minusDays(recordCount);
        createAndSaveService(HMCTS_SERVICE_CODE, serviceOnboardedDate);

        SittingRecord sittingRecord = createAndSaveSittingRecord(RECORDED,2L, Bruce_Wayne_ID,
                                                                 Bruce_Wayne);

        StatusHistory statusHistorySubmitted1 = createStatusHistory(SUBMITTED, Clark_Kent_ID, Clark_Kent);
        statusHistoryService.saveStatusHistory(statusHistorySubmitted1, sittingRecord);
        assertThat(sittingRecord.getId()).isNotNull();
        assertEquals(sittingRecord.getStatusId(), statusHistorySubmitted1.getStatusId());
        assertEquals(sittingRecord.getStatusHistories().size(), 2);

        createAndSaveSittingRecord(RECORDED, 2L, Peter_Parker_ID, Peter_Parker);

        SittingRecord sittingRecord3 = createAndSaveSittingRecord(RECORDED, 1L, Clark_Kent_ID,
                                                                  Clark_Kent);
        StatusHistory statusHistorySubmitted3 = createStatusHistory(SUBMITTED,
                                                                    Bruce_Wayne_ID, Bruce_Wayne);
        statusHistoryService.saveStatusHistory(statusHistorySubmitted3, sittingRecord3);
        assertEquals(sittingRecord3.getStatusId(), statusHistorySubmitted3.getStatusId());

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
        assertEquals(statusHistoryCreated1, actual.getFirstStatusHistory());
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldSetPotentialDuplicateRecordWhenJudgeRoleTypeIdDoesntMatch() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .contains(
                tuple(POTENTIAL_DUPLICATE_RECORD, USER_NAME_FIXED, RECORDED),
                tuple(POTENTIAL_DUPLICATE_RECORD, USER_NAME_FIXED, RECORDED),
                tuple(POTENTIAL_DUPLICATE_RECORD, USER_NAME_FIXED, RECORDED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldSetPotentialDuplicateRecordAndInvalidLocationWhenJudgeRoleTypeIdDoesntMatchAndLocationIsInvalid()
        throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers.stream()
                .skip(1)
                .forEach(sittingRecordWrapper -> sittingRecordWrapper.setErrorCode(INVALID_LOCATION));


        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .map(SittingRecordWrapper::getErrorCode,
                 SittingRecordWrapper::getCreatedByName,
                 SittingRecordWrapper::getStatusId)
            .containsExactly(tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                             tuple(INVALID_LOCATION, null, null),
                             tuple(INVALID_LOCATION, null, null)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .filteredOn(sittingRecordWrapper -> sittingRecordWrapper.getErrorCode() == POTENTIAL_DUPLICATE_RECORD)
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenJudgeRoleTypeIdDoesntMatchAndStatusSubmitted() throws IOException {
        repoRecordSittingRecords(JSON_RECORD_SITTING_RECORDS, PUBLISHED);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(INVALID_DUPLICATE_RECORD, "Recorder", PUBLISHED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", PUBLISHED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", PUBLISHED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }


    @Test
    void shouldSetValidRecordWhenEpimmsIdDoesntMatch() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordRequest> sittingRecordRequestList
            = recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(sittingRecordRequest -> sittingRecordRequest.toBuilder()
                .epimmsId("1000")
                .build())
            .toList();

        RecordSittingRecordRequest updatedRecordSittingRecordRequest = recordSittingRecordRequest.toBuilder()
            .recordedSittingRecords(sittingRecordRequestList)
            .build();


        List<SittingRecordWrapper> sittingRecordWrappers =
            updatedRecordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED)
            );
    }

    @Test
    void shouldSetValidRecordWhenPersonalCodeDontMatch() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordRequest> sittingRecordRequestList
            = recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(sittingRecordRequest -> sittingRecordRequest.toBuilder()
                .personalCode("NA")
                .build())
            .toList();

        RecordSittingRecordRequest updatedRecordSittingRecordRequest = recordSittingRecordRequest.toBuilder()
            .recordedSittingRecords(sittingRecordRequestList)
            .build();


        List<SittingRecordWrapper> sittingRecordWrappers =
            updatedRecordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenDurationDontMatch() throws IOException {
        List<SittingRecordWrapper> savedSittingRecordsWrapper = recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        List<SittingRecordRequest> sittingRecordRequestList
            = savedSittingRecordsWrapper.stream()
            .map(wrapper -> wrapper.getSittingRecordRequest().toBuilder()
                .durationBoolean(new DurationBoolean(
                    !wrapper.getSittingRecordRequest().getDurationBoolean().getAm(),
                    !wrapper.getSittingRecordRequest().getDurationBoolean().getPm()))
                .build())
            .toList();

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        RecordSittingRecordRequest updatedRecordSittingRecordRequest = recordSittingRecordRequest.toBuilder()
            .recordedSittingRecords(sittingRecordRequestList)
            .build();


        List<SittingRecordWrapper> sittingRecordWrappers =
            updatedRecordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenSittingDateDontMatch() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordRequest> sittingRecordRequestList
            = recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(sittingRecordRequest -> sittingRecordRequest.toBuilder()
                .sittingDate(sittingRecordRequest.getSittingDate().minusDays(100))
                .build())
            .toList();

        RecordSittingRecordRequest updatedRecordSittingRecordRequest = recordSittingRecordRequest.toBuilder()
            .recordedSittingRecords(sittingRecordRequestList)
            .build();


        List<SittingRecordWrapper> sittingRecordWrappers =
            updatedRecordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetPotentialDuplicateRecordWhenJudgeRoleTypeIdDoesntMatchWithReplaceDuplicateSetToTrue()
        throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(
            getResource("recordSittingRecordsPotentialDuplicateReplaceDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(POTENTIAL_DUPLICATE_RECORD, null, null),
                      tuple(POTENTIAL_DUPLICATE_RECORD, null, null),
                      tuple(POTENTIAL_DUPLICATE_RECORD, null, null)
            );
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenJudgeRoleTypeIdMatch() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource(JSON_RECORD_SITTING_RECORDS), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers
            .forEach(sittingRecordWrapper -> sittingRecordWrapper.setRegionId("1"));

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactly(tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenStatusRecordedDurationIntersect() throws IOException {
        recordSittingRecords(JSON_RECORD_SITTING_RECORDS);

        String requestJson = Resources.toString(getResource(JSON_RECORD_SITTING_RECORDS), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(sittingRecordRequest -> {
                    boolean am;
                    if (sittingRecordRequest.getDurationBoolean().getAm()
                        && sittingRecordRequest.getDurationBoolean().getPm()) {
                        am = false;
                    } else {
                        am = !sittingRecordRequest.getDurationBoolean().getAm();
                    }

                    return new SittingRecordWrapper(sittingRecordRequest.toBuilder()
                                                        .durationBoolean(
                                                            new DurationBoolean(am,
                                                                                sittingRecordRequest
                                                                                    .getDurationBoolean()
                                                                                    .getPm()))
                                                        .build());

                })
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactlyInAnyOrder(tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(VALID, null, null)
            );
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldSetInvalidDuplicateRecordWhenStatusNotRecordedAndDurationIntersect() throws IOException {
        repoRecordSittingRecords(JSON_RECORD_SITTING_RECORDS, PUBLISHED);

        String requestJson = Resources.toString(getResource(JSON_RECORD_SITTING_RECORDS), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(sittingRecordRequest -> {
                    boolean am;
                    if (sittingRecordRequest.getDurationBoolean().getAm()
                        && sittingRecordRequest.getDurationBoolean().getPm()) {
                        am = false;
                    } else {
                        am = !sittingRecordRequest.getDurationBoolean().getAm();
                    }

                    return new SittingRecordWrapper(sittingRecordRequest.toBuilder()
                                                        .durationBoolean(
                                                            new DurationBoolean(am,
                                                                                sittingRecordRequest
                                                                                    .getDurationBoolean()
                                                                                    .getPm()))
                                                        .build());

                })
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .containsExactlyInAnyOrder(tuple(INVALID_DUPLICATE_RECORD, "Recorder", PUBLISHED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", PUBLISHED),
                      tuple(VALID, null, null)
            );
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldReturnMedicalMembersOnlyQueriedRecordsWithMandatoryFieldsSet() {

        long counter = 7;
        LocalDate serviceOnboardedDate = LocalDate.now().minusDays(counter);
        createAndSaveService(HMCTS_SERVICE_CODE, serviceOnboardedDate);

        final String medicalJoh1 = "44";
        final String medicalJoh2 = "50";
        final List<String> medicalJohIds = List.of(medicalJoh1, medicalJoh2);
        final SittingRecord sittingRecord2 = createAndSaveSittingRecord(RECORDED,2L, USER_ID, USER_NAME, medicalJoh1);
        final SittingRecord sittingRecord4 = createAndSaveSittingRecord(RECORDED,4L, USER_ID, USER_NAME, medicalJoh2);
        createAndSaveSittingRecord(RECORDED,1L, USER_ID, USER_NAME, "nonMed1");
        createAndSaveSittingRecord(RECORDED,3L, USER_ID, USER_NAME, "nonMed2");
        createAndSaveSittingRecord(RECORDED,5L, USER_ID, USER_NAME, "nonMed3");
        createAndSaveSittingRecord(RECORDED,6L, USER_ID, USER_NAME, "nonMed4");
        createAndSaveSittingRecord(RECORDED,7L, USER_ID, USER_NAME, "nonMed5");

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord2.getRegionId())
            .epimmsId(sittingRecord2.getEpimmsId())
            .dateOrder(ASCENDING)
            .dateRangeFrom(serviceOnboardedDate)
            .dateRangeTo(LocalDate.now())
            .medicalMembersOnly(true)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord2.getHmctsServiceId()
        );

        uk.gov.hmcts.reform.jps.model.out.SittingRecord actual = response.get(0);
        assertThat(response).hasSize(2);
        assertTrue(medicalJohIds.contains(response.get(0).getJudgeRoleTypeId()));
        assertTrue(medicalJohIds.contains(response.get(1).getJudgeRoleTypeId()));
    }

    private Service createAndSaveService(String hmctsServiceCode, LocalDate serviceOnboardedDate) {
        Service service = Service.builder()
            .serviceName("servName1")
            .accountCenterCode("accCentr1")
            .hmctsServiceId(hmctsServiceCode)
            .onboardingStartDate(serviceOnboardedDate)
            .build();
        serviceService.save(service);


        Optional<Service> serviceRetrieved = serviceService.findService(hmctsServiceCode);
        LOGGER.debug("service: {}", serviceRetrieved.get());

        return serviceRetrieved.orElse(null);
    }

    private SittingRecord createAndSaveSittingRecord(StatusId statusId, Long counter, String userId, String userName) {
        return createAndSaveSittingRecord(statusId, counter, userId, userName, "HighCourt");
    }

    private SittingRecord createAndSaveSittingRecord(StatusId statusId, Long counter, String userId, String userName,
                                                     String judgeRoleTypeId) {
        SittingRecord sittingRecord = createSittingRecord(statusId,  counter, userId, userName, judgeRoleTypeId);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        LOGGER.debug("sittingRecord: {}", sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        assertThat(sittingRecord.getStatusHistories()).isNotNull();
        assertFalse(sittingRecord.getStatusHistories().isEmpty());
        return persistedSittingRecord;
    }

    private SittingRecord createSittingRecord(StatusId statusId, long counter, String userId, String userName,
                                              String judgeRoleTypeId) {
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
            .judgeRoleTypeId(judgeRoleTypeId)
            .build();
        StatusHistory statusHistory = createStatusHistory(sittingRecord.getStatusId(), userId, userName);
        sittingRecord.addStatusHistory(statusHistory);
        LOGGER.debug("sittingRecord: {}", sittingRecord);
        return sittingRecord;
    }


    private StatusHistory createStatusHistory(StatusId statusId, String userId, String userName) {
        StatusHistory statusHistory =  StatusHistory.builder()
            .statusId(statusId)
            .changedDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1))
            .changedByUserId(userId)
            .changedByName(userName)
            .build();

        LOGGER.debug("statusHistory: {}", statusHistory);

        return statusHistory;
    }

    private void createMultipleRecords(int count) {
        for (long i = count; i > 0; i--) {
            createAndSaveSittingRecord(RECORDED, i, USER_ID, USER_NAME);
        }
    }

    private List<SittingRecordWrapper> recordSittingRecords(String jsonRequest) throws IOException {
        String requestJson = Resources.toString(getResource(jsonRequest), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers
            .forEach(sittingRecordWrapper -> sittingRecordWrapper.setRegionId("1"));

        sittingRecordService.saveSittingRecords(HMCTS_SERVICE_CODE,
                                                sittingRecordWrappers,
                                                recordSittingRecordRequest.getRecordedByName(),
                                                recordSittingRecordRequest.getRecordedByIdamId());

        return sittingRecordWrappers;
    }

    private List<SittingRecordWrapper> repoRecordSittingRecords(String jsonRequest,
                                                                StatusId statusId) throws IOException {
        String requestJson = Resources.toString(getResource(jsonRequest), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers
            .forEach(sittingRecordWrapper -> sittingRecordWrapper.setRegionId("1"));

        sittingRecordWrappers
            .forEach(recordSittingRecordWrapper -> {
                SittingRecordRequest recordSittingRecord = recordSittingRecordWrapper.getSittingRecordRequest();
                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .sittingDate(recordSittingRecord.getSittingDate())
                        .statusId(statusId)
                        .regionId(recordSittingRecordWrapper.getRegionId())
                        .epimmsId(recordSittingRecord.getEpimmsId())
                        .hmctsServiceId(HMCTS_SERVICE_CODE)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getPm).orElse(false))
                        .build();

                recordSittingRecordWrapper.setCreatedDateTime(LocalDateTime.now());

                Arrays.stream(StatusId.values())
                    .filter(statusId1 -> statusId1 != DELETED && statusId1 != CLOSED)
                    .forEach(statusId1 -> {
                        StatusHistory statusHistory = StatusHistory.builder()
                            .statusId(statusId1)
                            .changedDateTime(LocalDateTime.now())
                            .changedByUserId(recordSittingRecordRequest.getRecordedByIdamId())
                            .changedByName(recordSittingRecordRequest.getRecordedByName())
                            .build();

                        sittingRecord.addStatusHistory(statusHistory);
                    });

                sittingRecordRepository.save(sittingRecord);
            });

        return sittingRecordWrappers;
    }
}
