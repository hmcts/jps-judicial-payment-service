package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.testcontainers.shaded.com.google.common.base.Function;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.time.LocalDate.of;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SittingRecordServiceTest {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String UPDATED_BY_USER_ID = UUID.randomUUID().toString();
    private static final LocalDateTime CURRENT_DATE_TIME = now();

    @Mock
    private SittingRecordRepository sittingRecordRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SittingRecordService sittingRecordService;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecordArgumentCaptor;

    private final Consumer<List<SittingRecordWrapper>> assertions = sittingRecordWrappers -> {
        assertThat(sittingRecordWrappers)
            .extracting("errorCode", "createdByName", "statusId")
            .contains(tuple(VALID, null, null),
                      tuple(VALID, null, null),
                      tuple(VALID, null, null));

        verify(sittingRecordRepository, times(3))
            .findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
                isA(LocalDate.class),
                isA(String.class),
                isA(String.class),
                eq(DELETED)
            );

        verify(statusHistoryRepository, never())
            .findFirstBySittingRecord(
                isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class),
                eq(Sort.sort(StatusHistory.class).by(StatusHistory::getId).descending()));
    };

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldReturnTotalRecordCount() {
        when(sittingRecordRepository.totalRecords(isA(SittingRecordSearchRequest.class),
                                                  isA(String.class)))
            .thenReturn(10);

        int totalRecordCount = sittingRecordService.getTotalRecordCount(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        assertThat(totalRecordCount)
            .isEqualTo(10);

    }

    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDb() {
        when(sittingRecordRepository.find(isA(SittingRecordSearchRequest.class),
                                                  isA(String.class)))
            .thenReturn(getDbSittingRecords(2));

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        assertThat(sittingRecords)
            .hasSize(2)
            .isEqualTo(getDomainSittingRecords(2));
    }


    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDbWithAmNull() {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = getDbSittingRecords(1);
        dbSittingRecords.get(0).setAm(false);

        when(sittingRecordRepository.find(isA(SittingRecordSearchRequest.class),
                                          isA(String.class)))
            .thenReturn(dbSittingRecords);

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        List<SittingRecord> domainSittingRecords = getDomainSittingRecords(1);
        domainSittingRecords.get(0).setAm(null);

        assertThat(sittingRecords)
            .hasSize(1)
            .isEqualTo(domainSittingRecords);
    }

    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDbWithPmNull() {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = getDbSittingRecords(1);
        dbSittingRecords.get(0).setPm(false);

        when(sittingRecordRepository.find(isA(SittingRecordSearchRequest.class),
                                          isA(String.class)))
            .thenReturn(dbSittingRecords);

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        List<SittingRecord> domainSittingRecords = getDomainSittingRecords(1);
        domainSittingRecords.get(0).setPm(null);

        assertThat(sittingRecords)
            .hasSize(1)
            .isEqualTo(domainSittingRecords);
    }

    private List<uk.gov.hmcts.reform.jps.domain.SittingRecord> getDbSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(count)
                .sittingDate(LocalDate.now().minusDays(2))
                .statusId(RECORDED)
                .regionId("1")
                .epimmsId("epims001")
                .hmctsServiceId("sscs")
                .personalCode("001")
                .contractTypeId(count)
                .judgeRoleTypeId("HighCourt")
                .am(true)
                .pm(true)
                .createdDateTime(CURRENT_DATE_TIME.minusDays(2))
                .createdByUserId(USER_ID)
                .changeByUserId(UPDATED_BY_USER_ID)
                .changeDateTime(CURRENT_DATE_TIME.minusDays(1))
                .build())
            .collect(Collectors.toList());
    }

    private List<SittingRecord> getDomainSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> SittingRecord.builder()
                .sittingRecordId(count)
                .sittingDate(LocalDate.now().minusDays(2))
                .statusId(RECORDED)
                .regionId("1")
                .epimmsId("epims001")
                .hmctsServiceId("sscs")
                .personalCode("001")
                .contractTypeId(count)
                .judgeRoleTypeId("HighCourt")
                .am(AM.name())
                .pm(PM.name())
                .createdByUserId(USER_ID)
                .createdDateTime(CURRENT_DATE_TIME.minusDays(2))
                .changeByUserId(UPDATED_BY_USER_ID)
                .changeDateTime(CURRENT_DATE_TIME.minusDays(1))
                .build())
            .collect(Collectors.toList());
    }

    @Test
    void shouldSaveSittingRecordsWhenRequestIsValid() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(SittingRecordWrapper::new)
            .toList();

        sittingRecordService.saveSittingRecords("test",
                                                sittingRecordWrappers,
                                                recordSittingRecordRequest.getRecordedByName(),
                                                recordSittingRecordRequest.getRecordedByIdamId());

        verify(sittingRecordRepository, times(3))
            .save(sittingRecordArgumentCaptor.capture());

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords = sittingRecordArgumentCaptor.getAllValues();
        assertThat(sittingRecords).extracting("sittingDate", "statusId", "epimmsId", "hmctsServiceId",
                                              "personalCode", "contractTypeId", "judgeRoleTypeId", "am", "pm")
                .contains(
                    tuple(of(2023, Month.MAY, 11), RECORDED, "852649", "test", "4918178", 1L, "Judge", false, true),
                    tuple(of(2023, Month.APRIL, 10), RECORDED, "852649", "test", "4918178", 1L, "Judge", true, false),
                    tuple(of(2023, Month.MARCH, 9), RECORDED, "852649", "test", "4918178", 1L, "Judge", true, true)
        );

        assertThat(sittingRecords).flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .extracting("statusId", "changeByUserId", "changeByName")
            .contains(
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder")
        );

        assertThat(sittingRecords).describedAs("Created date assertion")
            .flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangeDateTime()));
    }

    @Test
    void shouldSetPotentialDuplicateRecordWhenJudgeRoleTypeIdDoesntMatch() throws IOException {
        Consumer<List<SittingRecordWrapper>> assertions = sittingRecordWrappers -> {
            assertThat(sittingRecordWrappers)
                .extracting("errorCode", "createdByName", "statusId")
                .contains(
                    tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                    tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED),
                    tuple(POTENTIAL_DUPLICATE_RECORD, "Recorder", RECORDED)
                );

            assertThat(sittingRecordWrappers).describedAs("Created date assertion")
                .allMatch(sittingRecordWrapper -> LocalDateTime.now().minusMinutes(5)
                    .isBefore(sittingRecordWrapper.getCreatedDateTime()));

            verify(sittingRecordRepository, times(3))
                .findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
                    isA(LocalDate.class),
                    isA(String.class),
                    isA(String.class),
                    eq(DELETED)
                );

            verify(statusHistoryRepository, times(3))
                .findFirstBySittingRecord(
                    isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class),
                    eq(Sort.sort(StatusHistory.class).by(StatusHistory::getId).descending()));
        };

        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    sittingRecordRequest.getPersonalCode(),
                                                    sittingRecordRequest.getDurationBoolean().getAm(),
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    "Tester"),
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetInvalidDuplicateRecordWhenDurationDontMatch() throws IOException {
        Consumer<List<SittingRecordWrapper>> assertions = sittingRecordWrappers -> {
            assertThat(sittingRecordWrappers)
                .extracting("errorCode", "createdByName", "statusId")
                .contains(
                    tuple(INVALID_DUPLICATE_RECORD, null, null),
                    tuple(INVALID_DUPLICATE_RECORD, null, null),
                    tuple(VALID, null, null)
                );

            verify(sittingRecordRepository, times(3))
                .findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
                    isA(LocalDate.class),
                    isA(String.class),
                    isA(String.class),
                    eq(DELETED)
                );
        };

        execute(sittingRecordRequest -> {
            boolean am;
            if (sittingRecordRequest.getDurationBoolean().getAm()
                && sittingRecordRequest.getDurationBoolean().getPm()) {
                am = false;
            } else {
                am = !sittingRecordRequest.getDurationBoolean().getAm();
            }
            return getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    sittingRecordRequest.getPersonalCode(),
                                                    am,
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    sittingRecordRequest.getJudgeRoleTypeId());
            },
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetValidWhenEpimmsIdDoentMatch() throws IOException {
        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    "Test",
                                                    sittingRecordRequest.getPersonalCode(),
                                                    sittingRecordRequest.getDurationBoolean().getAm(),
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    sittingRecordRequest.getJudgeRoleTypeId()),
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetValidWhenPersonalCodeDontMatch() throws IOException {
        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    "tester",
                                                    sittingRecordRequest.getDurationBoolean().getAm(),
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    sittingRecordRequest.getJudgeRoleTypeId()),
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetValidRecordWhenDurationDontMatch() throws IOException {
        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    sittingRecordRequest.getPersonalCode(),
                                                    !sittingRecordRequest.getDurationBoolean().getAm(),
                                                    !sittingRecordRequest.getDurationBoolean().getPm(),
                                                    sittingRecordRequest.getJudgeRoleTypeId()),
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetValidRecordWhenSittingDateDontMatch() throws IOException {
        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate().minusDays(100),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    sittingRecordRequest.getPersonalCode(),
                                                    sittingRecordRequest.getDurationBoolean().getAm(),
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    sittingRecordRequest.getJudgeRoleTypeId()),
                assertions,
                "recordSittingRecordsPotentialDuplicate.json"
        );
    }

    @Test
    void shouldSetValidRecordWhenJudgeRoleTypeIdDoesntMatchWithReplaceDuplicateSetToTrue() throws IOException {

        Consumer<List<SittingRecordWrapper>> assertions = sittingRecordWrappers -> {
            assertThat(sittingRecordWrappers)
                .extracting("errorCode", "createdByName", "statusId", "delete")
                .contains(tuple(VALID, null, null, true),
                          tuple(VALID, null, null, true),
                          tuple(VALID, null, null, true)
                );

            verify(sittingRecordRepository, times(3))
                .findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
                    isA(LocalDate.class),
                    isA(String.class),
                    isA(String.class),
                    eq(DELETED)
                );

            verify(statusHistoryRepository, never())
                .findFirstBySittingRecord(
                    isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class),
                    eq(Sort.sort(StatusHistory.class).by(StatusHistory::getId).descending()));
        };

        execute(sittingRecordRequest -> getDbRecord(sittingRecordRequest.getSittingDate(),
                                                    sittingRecordRequest.getEpimmsId(),
                                                    sittingRecordRequest.getPersonalCode(),
                                                    sittingRecordRequest.getDurationBoolean().getAm(),
                                                    sittingRecordRequest.getDurationBoolean().getPm(),
                                                    "test"),
                assertions,
                "recordSittingRecordsReplaceDuplicate.json"
        );

    }

    void execute(Function<SittingRecordRequest, SittingRecordDuplicateCheckFields> getDbRecord,
                 Consumer<List<SittingRecordWrapper>> assertions, String resourceName) throws IOException {

        String requestJson = Resources.toString(getResource(resourceName), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordDuplicateCheckFields> dbSittingRecordDuplicateCheckFields = getDbRecord(
            recordSittingRecordRequest,
            getDbRecord
        );

        when(sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            any(), any(), any(), any())
        ).thenReturn(Streamable.of(dbSittingRecordDuplicateCheckFields));

        when(statusHistoryRepository.findFirstBySittingRecord(any(), any()))
            .thenReturn(Optional.of(StatusHistory.builder()
                                        .changeByName("Recorder")
                                        .changeDateTime(LocalDateTime.now().minusSeconds(30))
                                        .statusId(RECORDED)
                                        .build()));

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordWrappers
            .forEach(sittingRecordRequest -> sittingRecordRequest.setRegionId("1"));

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        assertions.accept(sittingRecordWrappers);
    }

    private List<SittingRecordDuplicateCheckFields> getDbRecord(
        RecordSittingRecordRequest recordSittingRecordRequest,
        Function<SittingRecordRequest, SittingRecordDuplicateCheckFields> getSittingRecordFromDb) {

        return recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(getSittingRecordFromDb::apply)
            .toList();
    }

    private SittingRecordDuplicateCheckFields getDbRecord(LocalDate sittingDate,
                                                          String epimmsId,
                                                          String personalCode,
                                                          Boolean am,
                                                          Boolean pm,
                                                          String judgeRoleTypeId) {
        return new SittingRecordDuplicateCheckFields() {

            @Override
            public Long getId() {
                return new Random().nextLong();
            }

            @Override
            public LocalDate getSittingDate() {
                return sittingDate;
            }

            @Override
            public String getEpimmsId() {
                return epimmsId;
            }

            @Override
            public String getPersonalCode() {
                return personalCode;
            }

            @Override
            public Boolean getAm() {
                return am;
            }

            @Override
            public Boolean getPm() {
                return pm;
            }

            @Override
            public StatusId getStatusId() {
                return RECORDED;
            }

            @Override
            public String getJudgeRoleTypeId() {
                return judgeRoleTypeId;
            }
        };
    }
}
