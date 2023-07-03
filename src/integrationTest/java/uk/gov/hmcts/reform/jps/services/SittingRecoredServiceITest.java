package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.components.EvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateMatchingDuration;
import uk.gov.hmcts.reform.jps.components.EvaluateOverlapDuration;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
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
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.DateOrder.ASCENDING;
import static uk.gov.hmcts.reform.jps.model.DateOrder.DESCENDING;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SittingRecoredServiceITest extends BaseTest {
    public static final String EPIM_ID = "123";
    public static final String SSC_ID = "ssc_id";
    public static final String CONTRACT_TYPE_ID = "contractTypeId";
    public static final String CREATED_BY_USER_ID = "createdByUserId";
    @Autowired
    private SittingRecordRepository sittingRecordRepository;
    @Autowired
    private StatusHistoryRepository statusHistoryRepository;
    @Autowired
    private EvaluateDuplicate evaluateDuplicate;
    @Autowired
    private EvaluateMatchingDuration evaluateMatchingDuration;
    @Autowired
    private EvaluateOverlapDuration evaluateOverlapDuration;
    @Autowired
    private StatusHistoryService statusHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private SittingRecordService sittingRecordService;
    private static final String USER_ID = UUID.randomUUID().toString();

    @BeforeEach
    void beforeEach() {
        sittingRecordService = new SittingRecordService(
            sittingRecordRepository,
            evaluateDuplicate,
            evaluateMatchingDuration,
            evaluateOverlapDuration,
            statusHistoryService
        );
    }

    @AfterEach
    void afterEach() {
        sittingRecordRepository.deleteAll();
    }

    @Test
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

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


        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = getSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);
        assertThat(expected).isEqualTo(response.get(0));
    }

    private uk.gov.hmcts.reform.jps.model.out.SittingRecord getSittingRecord(
        SittingRecord sittingRecord) {
        String notSet = null;
        return Optional.ofNullable(sittingRecord)
            .map(persistedSittingRecord -> uk.gov.hmcts.reform.jps.model.out.SittingRecord.builder()
            .sittingRecordId(persistedSittingRecord.getId())
            .sittingDate(persistedSittingRecord.getSittingDate())
            .statusId(persistedSittingRecord.getStatusId())
            .regionId(persistedSittingRecord.getRegionId())
            .epimmsId(persistedSittingRecord.getEpimmsId())
            .hmctsServiceId(persistedSittingRecord.getHmctsServiceId())
            .personalCode(persistedSittingRecord.getPersonalCode())
            .contractTypeId(persistedSittingRecord.getContractTypeId())
            .judgeRoleTypeId(persistedSittingRecord.getJudgeRoleTypeId())
            .am(persistedSittingRecord.isAm() ? AM.name() : notSet)
            .pm(persistedSittingRecord.isPm() ? PM.name() : notSet)
            .createdDateTime(persistedSittingRecord.getCreatedDateTime())
            .createdByUserId(persistedSittingRecord.getCreatedByUserId())
            .changeDateTime(persistedSittingRecord.getChangeDateTime())
            .changeByUserId(persistedSittingRecord.getChangeByUserId())
            .build())
            .orElseThrow();
    }

    private  SittingRecord getSittingRecord(long counter) {
        SittingRecord.SittingRecordBuilder builder = SittingRecord.builder();
        return builder
            .sittingDate(LocalDate.now().minusDays(counter))
            .statusId(RECORDED)
            .regionId("1")
            .epimmsId(EPIM_ID)
            .hmctsServiceId(SSC_ID)
            .personalCode("001")
            .contractTypeId(counter)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
            .createdByUserId(USER_ID)
            .build();
    }


    @Test
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2);
        SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

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

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = getSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);
        assertThat(expected).isEqualTo(response.get(0));
    }

    private void createMultipleRecords(int count) {
        for (long i = count; i > 0; i--) {
            SittingRecord sittingRecord = getSittingRecord(i);
            SittingRecord persistedSittingRecord = sittingRecordRepository.save(sittingRecord);
            assertThat(persistedSittingRecord).isNotNull();
        }
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
            .epimmsId(EPIM_ID)
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
    void shouldReturnLast2RecordsWhenSortOrderIsDecending() {
        int recordCount = 22;
        String reasonId = "1";

        createMultipleRecords(recordCount);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimmsId(EPIM_ID)
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
            .epimmsId(EPIM_ID)
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
        recordSittingRecords("recordSittingRecords.json");
        List<SittingRecord> savedSittingRecords = sittingRecordRepository.findAll();

        assertThat(savedSittingRecords)
            .extracting("sittingDate","regionId",  "epimmsId", "personalCode", "judgeRoleTypeId", "contractTypeId",
                        "am", "pm", "statusId", "hmctsServiceId")
            .contains(
                tuple(of(2023, MAY, 11), "1", "852649", "4918178", "Judge", 1L, false, true, RECORDED, "ssc_id"),
                tuple(of(2023, APRIL,10), "1", "852649", "4918178", "Judge", 1L, true, false, RECORDED, "ssc_id"),
                tuple(of(2023, MARCH,9), "1", "852649", "4918178", "Judge", 1L, true, true, RECORDED, "ssc_id")
            );

        List<StatusHistory> statusHistories = statusHistoryRepository.findAll();
        assertThat(statusHistories)
            .extracting("statusId", "changeByUserId", "changeByName")
            .contains(
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder")
            );

        assertThat(statusHistories).describedAs("Created date assertion")
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangeDateTime()));
    }

    @Test
    void shouldSetPotentialDuplicateRecordWhenJudgeRoleTypeIdDoesntMatch() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenJudgeRoleTypeIdDoesntMatchAndStatusSubmitted() throws IOException {
        repoRecordSittingRecords("recordSittingRecords.json", SUBMITTED);

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
            .contains(tuple(INVALID_DUPLICATE_RECORD, "Recorder", SUBMITTED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", SUBMITTED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", SUBMITTED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }


    @Test
    void shouldSetValidRecordWhenEpimmsIdDoesntMatch() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenPersonalCodeDontMatch() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenDurationDontMatch() throws IOException {
        List<SittingRecordWrapper> savedSittingRecordsWrapper = recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenSittingDateDontMatch() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetValidRecordWhenJudgeRoleTypeIdDoesntMatchWithReplaceDuplicateSetToTrue() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

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
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenJudgeRoleTypeIdMatch() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
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
            .contains(tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", RECORDED)
            );

        assertThat(sittingRecordWrappers).describedAs("Created date assertion")
            .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                .isBefore(sittingRecordWrapper.getCreatedDateTime()));
    }

    @Test
    void shouldSetPotentialDuplicateRecordWhenStatusRecordedDurationIntersect() throws IOException {
        recordSittingRecords("recordSittingRecords.json");

        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
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
            .contains(tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                      tuple(VALID, null, null)
            );
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenStatusNotRecordedAndDurationIntersect() throws IOException {
        repoRecordSittingRecords("recordSittingRecords.json", SUBMITTED);

        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
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
            .contains(tuple(INVALID_DUPLICATE_RECORD, "Recorder", SUBMITTED),
                      tuple(INVALID_DUPLICATE_RECORD, "Recorder", SUBMITTED),
                      tuple(VALID, null, null)
            );
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

        sittingRecordService.saveSittingRecords(SSC_ID,
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
                        .hmctsServiceId(SSC_ID)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getPm).orElse(false))
                        .build();

                recordSittingRecordWrapper.setCreatedDateTime(LocalDateTime.now());

                StatusHistory statusHistory = StatusHistory.builder()
                    .statusId(statusId)
                    .changeDateTime(LocalDateTime.now())
                    .changeByUserId(recordSittingRecordRequest.getRecordedByIdamId())
                    .changeByName(recordSittingRecordRequest.getRecordedByName())
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);
            });

        return sittingRecordWrappers;
    }

    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnCountOfRecordsSubmittedWhenMatchRecordFoundInSittingRecordsTable() {
        SubmitSittingRecordRequest submitSittingRecordRequest = SubmitSittingRecordRequest.builder()
            .submittedByIdamId("b139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .submittedByName("submitter")
            .regionId("4")
            .dateRangeFrom(LocalDate.parse("2023-05-11"))
            .dateRangeTo(LocalDate.parse("2023-05-11"))
            .createdByUserId("d139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .build();


        int countSubmitted = sittingRecordService.submitSittingRecords(
            submitSittingRecordRequest,
            "BBA3"
        );
        assertThat(countSubmitted)
            .isEqualTo(1);
    }
}
