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
import org.springframework.data.util.Streamable;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.components.BaseEvaluateDuplicate;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ForbiddenException;
import uk.gov.hmcts.reform.jps.model.RecordSubmitFields;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SubmitSittingRecordResponse;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.time.LocalDate.of;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_LOCATION;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.CLOSED;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SittingRecordServiceTest extends BaseEvaluateDuplicate {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String UPDATED_BY_USER_ID = UUID.randomUUID().toString();

    private static final Long ID = new Random().nextLong();
    public static final String HMCTS_SERVICE_CODE = "test";
    public static final String EPIMMS_ID = "epimms001";
    public static final String LOCATION = "Sutton Social Security";

    @Mock
    private SittingRecordRepository sittingRecordRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private DuplicateCheckerService duplicateCheckerService;

    @Mock
    private LocationService locationService;

    @Mock
    private ServiceService serviceService;

    @Mock
    private StatusHistoryService statusHistoryService;

    @Mock
    private JudicialOfficeHolderService judicialOfficeHolderService;

    @Mock
    private ApplicationProperties properties;

    @InjectMocks
    private SittingRecordService sittingRecordService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecordArgumentCaptor;
    @Captor
    private ArgumentCaptor<Long> records;
    @Captor
    private ArgumentCaptor<String> personalCode;

    @Captor
    private ArgumentCaptor<LocalDate> sittingDate;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        when(locationService.getVenueName(HMCTS_SERVICE_CODE, EPIMMS_ID))
            .thenReturn(LOCATION);
        when(serviceService.findService(HMCTS_SERVICE_CODE))
            .thenReturn(Optional.of(Service.builder()
                                        .accountCenterCode("123")
                                        .onboardingStartDate(LocalDate.now().minusYears(4))
                                        .build()));
    }

    @Test
    void shouldReturnTotalRecordCount() {
        when(sittingRecordRepository.totalRecords(isA(SittingRecordSearchRequest.class),
                                                  isA(String.class),
                                                  any(), any()))
            .thenReturn(10L);
        when(properties.getMedicalJohRoleIds()).thenReturn(List.of("Test1","Test2"));

        long totalRecordCount = sittingRecordService.getTotalRecordCount(
            SittingRecordSearchRequest.builder().build(),
            HMCTS_SERVICE_CODE
        );

        assertThat(totalRecordCount)
            .isEqualTo(10);
    }

    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDb() {
        when(sittingRecordRepository.find(isA(SittingRecordSearchRequest.class),
                                                  isA(String.class),
                                          any(LocalDate.class), anyList()))
            .thenReturn(getDbSittingRecords(2).stream());

        when(serviceService.getServiceDateOnboarded(anyString()))
            .thenReturn(LocalDate.now().minusDays(5));

        when(locationService.getCourtVenues(anyString()))
            .thenReturn(List.of(
                CourtVenue.builder()
                    .epimmsId("1")
                    .regionId("1")
                    .siteName("one")
                    .build())
            );

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            HMCTS_SERVICE_CODE
        );

        assertThat(sittingRecords).hasSize(2);

        assertThat(sittingRecords.get(0)).isEqualTo(getDomainSittingRecords(2).get(0));
        assertThat(sittingRecords.get(1)).isEqualTo(getDomainSittingRecords(2).get(1));
        verify(serviceService).findService(anyString());
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

        sittingRecordWrappers
            .forEach(sittingRecordWrapper -> sittingRecordWrapper.setRegionId("1"));

        sittingRecordService.saveSittingRecords(
            HMCTS_SERVICE_CODE,
            sittingRecordWrappers,
            recordSittingRecordRequest.getRecordedByName(),
            recordSittingRecordRequest.getRecordedByIdamId());

        verify(sittingRecordRepository, times(3))
            .save(sittingRecordArgumentCaptor.capture());

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords = sittingRecordArgumentCaptor.getAllValues();
        assertThat(sittingRecords).extracting(SittingRecord_.SITTING_DATE, SittingRecord_.STATUS_ID,
                                              SittingRecord_.EPIMMS_ID, SittingRecord_.HMCTS_SERVICE_ID,
                                              SittingRecord_.PERSONAL_CODE, SittingRecord_.CONTRACT_TYPE_ID,
                                              SittingRecord_.JUDGE_ROLE_TYPE_ID, SittingRecord_.AM, SittingRecord_.PM)
                .contains(
                    tuple(of(2022, Month.MAY, 11), RECORDED, "852649",
                          HMCTS_SERVICE_CODE, "4918500", 1L, "Tester", false, true),
                    tuple(of(2023, Month.APRIL, 10), RECORDED, "852649",
                          HMCTS_SERVICE_CODE, "4918179", 1L, "Judge", true, false),
                    tuple(of(2023, Month.MARCH, 9), RECORDED, "852649",
                          HMCTS_SERVICE_CODE, "4918180", 1L, "Judge", true, true)
        );

        assertThat(sittingRecords).flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .extracting("statusId", "changedByUserId", "changedByName")
            .contains(
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple(RECORDED, "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder")
        );

        assertThat(sittingRecords).describedAs("Created date assertion")
            .flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangedDateTime()));
    }

    private List<uk.gov.hmcts.reform.jps.domain.SittingRecord> getDbSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(count)
                .sittingDate(LocalDate.now().minusDays(2))
                .statusId(RECORDED)
                .regionId("1")
                .epimmsId(EPIMMS_ID)
                .hmctsServiceId("sscs")
                .personalCode("001")
                .contractTypeId(count)
                .judgeRoleTypeId("HighCourt")
                .am(true)
                .pm(true)
                .build())
            .toList();
    }

    private List<SittingRecord> getDomainSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> SittingRecord.builder()
                    .sittingRecordId(count)
                    .sittingDate(LocalDate.now().minusDays(2))
                    .statusId(RECORDED)
                    .regionId("1")
                    .epimmsId(EPIMMS_ID)
                    .hmctsServiceId("sscs")
                    .personalCode("001")
                    .contractTypeId(count)
                    .judgeRoleTypeId("HighCourt")
                    .am(true)
                    .pm(true)
                    .build())
            .collect(Collectors.toList());
    }


    private uk.gov.hmcts.reform.jps.domain.SittingRecord deleteTestSetUp(String changeById, StatusId state) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(state)
            .changedDateTime(now())
            .changedByUserId(changeById)
            .changedByName("John Smith")
            .build();

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
            .id(ID)
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(state)
            .regionId("1")
            .epimmsId(EPIMMS_ID)
            .hmctsServiceId("sscs")
            .personalCode("001")
            .contractTypeId(1L)
            .judgeRoleTypeId("HighCourt")
            .am(true)
            .pm(true)
            .build();

        sittingRecord.addStatusHistory(statusHistory);

        return sittingRecord;
    }

    @Test
    void shouldDeleteRecordRecorder() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, RECORDED);

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));
        when(sittingRecordRepository.findRecorderSittingRecord(ID, DELETED))
                .thenReturn(Optional.of(sittingRecord));

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory
            = sittingRecord.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangedDateTime));

        assertThat(optionalStatusHistory)
            .map(StatusHistory::getStatusId)
            .hasValue(DELETED);

    }

    @Test
    void shouldDeleteRecordSubmitter() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(
            UPDATED_BY_USER_ID,
            RECORDED
        );

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory
            = sittingRecord.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangedDateTime));

        assertThat(optionalStatusHistory)
            .map(StatusHistory::getStatusId)
            .hasValue(DELETED);
    }

    @Test
    void shouldDeleteRecordAdmin() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(
            UPDATED_BY_USER_ID,
            SUBMITTED
        );

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory
            = sittingRecord.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangedDateTime));
        assertThat(optionalStatusHistory)
            .map(StatusHistory::getStatusId)
            .hasValue(DELETED);
    }

    @Test
    void differentRecorderID() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
            deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED);

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        Exception exception = assertThrows(ForbiddenException.class, () ->
            sittingRecordService.deleteSittingRecord(ID)
        );

        Optional<StatusHistory> statusHistory = sittingRecord.getLatestStatusHistory();
        assertThat(statusHistory)
            .map(StatusHistory::getStatusId)
            .hasValue(RECORDED);
        assertThat(exception.getMessage())
            .isEqualTo("User IDAM ID does not match the oldest Changed by IDAM ID ");
    }

    @Test
    void wrongStateRecorder() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, SUBMITTED);

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        Long id = sittingRecord.getId();
        Exception exception = assertThrows(ForbiddenException.class, () ->
            sittingRecordService.deleteSittingRecord(id)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Sitting Record Status ID is in wrong state");
    }

    @Test
    void wrongStateSubmitter() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, SUBMITTED);

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        Long id = sittingRecord.getId();
        Exception exception = assertThrows(ForbiddenException.class, () ->
            sittingRecordService.deleteSittingRecord(id)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Sitting Record Status ID is in wrong state");
    }

    @Test
    void wrongStateAdmin() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, RECORDED);

        when(sittingRecordRepository.findRecorderSittingRecord(sittingRecord.getId(), DELETED))
                .thenReturn(Optional.of(sittingRecord));

        Long id = sittingRecord.getId();
        Exception exception = assertThrows(ForbiddenException.class, () ->
            sittingRecordService.deleteSittingRecord(id)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Sitting Record Status ID is in wrong state");
    }


    @Test
    void shouldInvokeDuplicateCheckerWhenMatchingRecordsFoundInDb() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
            sittingRecordRequest.getSittingDate(),
            sittingRecordRequest.getEpimmsId(),
            sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            "Tester",
            RECORDED
        );

        when(sittingRecordRepository.findBySittingDateAndPersonalCodeAndStatusIdNotIn(
            any(), any(), anyList())
        ).thenReturn(Streamable.of(List.of(sittingRecordDuplicateCheckFields)));


        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        verify(duplicateCheckerService, times(3)).evaluate(any(), any());
    }

    @Test
    void shouldInvokeDuplicateCheckerOncePerSittingRecordRequestWhenMoreThanOneMatchingRecordsFoundInDb()
        throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecordsOnePotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields firstDbRecord
            = getDbRecord(
            sittingRecordRequest.getSittingDate(),
            sittingRecordRequest.getEpimmsId(),
            sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            "3",
            RECORDED
        );

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields secondDbRecord
            = getDbRecord(
            sittingRecordRequest.getSittingDate(),
            sittingRecordRequest.getEpimmsId(),
            sittingRecordRequest.getPersonalCode(),
            !sittingRecordRequest.getDurationBoolean().getAm(),
            !sittingRecordRequest.getDurationBoolean().getPm(),
            "14",
            RECORDED
        );

        when(sittingRecordRepository.findBySittingDateAndPersonalCodeAndStatusIdNotIn(
            any(), any(), anyList())
        ).thenReturn(
            Streamable.of(
                List.of(
                    firstDbRecord,
                    secondDbRecord
                )
            )
        );

        doAnswer(invocation -> {
            SittingRecordWrapper sittingRecordWrapper = invocation.getArgument(0);
            sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
            return null;
        }).when(duplicateCheckerService).evaluate(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class));


        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        verify(duplicateCheckerService, times(1))
            .evaluate(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class)
            );
    }

    @Test
    void shouldNotInvokeDuplicateCheckerWhenSittingRecordWrapperIsInValid() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(sittingRecordRequest -> SittingRecordWrapper.builder()
                    .sittingRecordRequest(sittingRecordRequest)
                    .errorCode(INVALID_LOCATION)
                    .build())
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        verify(duplicateCheckerService, never()).evaluate(any(), any());
    }

    @Test
    void shouldNotInvokeDuplicateCheckerWhenNoMatchingRecordsFoundInDb() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordDuplicateCheckFields> dbSittingRecordDuplicateCheckFields = Collections.emptyList();

        when(sittingRecordRepository.findBySittingDateAndPersonalCodeAndStatusIdNotIn(
            any(), any(),  anyList())
        ).thenReturn(Streamable.of(dbSittingRecordDuplicateCheckFields));

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        verify(duplicateCheckerService, never()).evaluate(any(), any());
    }

    @Test
    void shouldReturnCountOfRecordsSubmittedWhenMatchRecordFoundInSittingRecordsTable() {
        LocalDate now = LocalDate.now();
        String hmctsServiceCode = "BBA3";
        List<RecordSubmitFields> sittingRecordIds = List.of(
            getRecordSubmitFields(1L, 6L, "123", now.minusDays(2)),
            getRecordSubmitFields(100L, 2L, "243", now.minusDays(3)),
            getRecordSubmitFields(200L, 3L, "567", now.minusDays(4)),
            getRecordSubmitFields(300L, 6L, "789", now.minusDays(5)),
            getRecordSubmitFields(400L, 6L, "999", now.minusDays(6))
        );

        when(judicialOfficeHolderService.getCrownServiceFlag("123", now.minusDays(2)))
            .thenReturn(Optional.of(true));
        when(judicialOfficeHolderService.getCrownServiceFlag("789", now.minusDays(5)))
            .thenReturn(Optional.of(false));
        when(judicialOfficeHolderService.getCrownServiceFlag("999", now.minusDays(6)))
            .thenReturn(Optional.empty());

        SubmitSittingRecordRequest submitSittingRecordRequest = SubmitSittingRecordRequest.builder()
            .submittedByIdamId("b139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .submittedByName("submitter")
            .regionId("4")
            .dateRangeFrom(LocalDate.parse("2023-05-11"))
            .dateRangeTo(LocalDate.parse("2023-05-11"))
            .createdByUserId("d139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .build();

        when(sittingRecordRepository.findRecordsToSubmit(submitSittingRecordRequest,
                                                         hmctsServiceCode))
            .thenReturn(sittingRecordIds);

        SubmitSittingRecordResponse submitSittingRecordResponse = sittingRecordService.submitSittingRecords(
            submitSittingRecordRequest,
            hmctsServiceCode,
            serviceService.getServiceDateOnboarded(hmctsServiceCode)
        );

        verify(statusHistoryService, times(2))
            .insertRecord(records.capture(),
                          eq(SUBMITTED),
                          eq(submitSittingRecordRequest.getSubmittedByIdamId()),
                          eq(submitSittingRecordRequest.getSubmittedByName()));

        assertThat(records.getAllValues())
            .describedAs("Records submitted")
            .contains(1L, 100L);

        verify(statusHistoryService, times(2))
            .insertRecord(records.capture(),
                          eq(CLOSED),
                          eq(submitSittingRecordRequest.getSubmittedByIdamId()),
                          eq(submitSittingRecordRequest.getSubmittedByName()));

        assertThat(records.getAllValues())
            .describedAs("Records deleted")
            .contains(200L);


        assertThat(submitSittingRecordResponse.getRecordsSubmitted())
            .isEqualTo(2);

        assertThat(submitSittingRecordResponse.getRecordsClosed())
            .isEqualTo(2);


        verify(sittingRecordRepository, times(4))
            .updateRecordedStatus(anyLong(), isA(StatusId.class));
        verify(judicialOfficeHolderService, times(5))
            .getCrownServiceFlag(personalCode.capture(), sittingDate.capture());

        assertThat(personalCode.getAllValues())
            .containsExactlyInAnyOrder("123",
                                       "789",
                                       "999",
                                       "789",
                                       "999");

        assertThat(sittingDate.getAllValues())
            .containsExactlyInAnyOrder(now.minusDays(2),
                                       now.minusDays(5),
                                       now.minusDays(6),
                                       now.minusDays(5),
                                       now.minusDays(6));


    }

    private static RecordSubmitFields getRecordSubmitFields(
        long id,
        long contractTypeId,
        String number,
        LocalDate sittingDate) {

        return RecordSubmitFields.builder()
            .id(id)
            .contractTypeId(contractTypeId)
            .personalCode(number)
            .sittingDate(sittingDate)
            .build();
    }

    @Test
    void shouldReturnZeroRecordsSubmittedWhenNoRecordFound() {
        String hmctsServiceCode = "BBA3";
        SubmitSittingRecordRequest submitSittingRecordRequest = SubmitSittingRecordRequest.builder()
            .submittedByIdamId("b139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .submittedByName("submitter")
            .regionId("4")
            .dateRangeFrom(LocalDate.parse("2023-05-11"))
            .dateRangeTo(LocalDate.parse("2023-05-11"))
            .createdByUserId("d139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .build();

        when(sittingRecordRepository.findRecordsToSubmit(submitSittingRecordRequest,
                                                         hmctsServiceCode))
            .thenReturn(Collections.emptyList());

        SubmitSittingRecordResponse submitSittingRecordResponse = sittingRecordService.submitSittingRecords(
            submitSittingRecordRequest,
            hmctsServiceCode,
            serviceService.getServiceDateOnboarded(hmctsServiceCode)
        );
        verify(statusHistoryService, never())
            .insertRecord(anyLong(),
                          eq(SUBMITTED),
                          eq(submitSittingRecordRequest.getSubmittedByIdamId()),
                          eq(submitSittingRecordRequest.getSubmittedByName()));

        verify(sittingRecordRepository, never())
            .updateRecordedStatus(any(), isA(StatusId.class));

        assertThat(submitSittingRecordResponse.getRecordsSubmitted())
            .isZero();
        assertThat(submitSittingRecordResponse.getRecordsClosed())
            .isZero();
    }

    @Test
    void shouldRecordSittingRecordsWhenPotentialDuplicateRecord() throws IOException {
        long sittingRecordId = 9L;
        String requestJson = Resources.toString(getResource("singleSittingRecordWithPotentialDuplicate.json"), UTF_8);
        requestJson = requestJson.replace("<flag>", "true");
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                        .map(SittingRecordWrapper::new)
                        .peek(sittingRecordWrapper -> {
                            sittingRecordWrapper.setSittingRecordId(sittingRecordId);
                            sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                        })
                        .toList();
        when(sittingRecordRepository.findById(anyLong()))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.jps.domain.SittingRecord.builder().build()));
        UserInfo userInfo = UserInfo.builder()
            .uid(USER_ID)
            .givenName("Judge")
            .build();
        when(securityUtils.getUserInfo()).thenReturn(userInfo);

        sittingRecordService.saveSittingRecords("SSC_ID",
                sittingRecordWrappers,
                recordSittingRecordRequest.getRecordedByName(),
                recordSittingRecordRequest.getRecordedByIdamId());

        verify(sittingRecordRepository, times(2)).save(isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class));
    }

    @Test
    void shouldNotDeleteRecordSittingRecordsWhenPotentialDuplicateRecordIsNotSet() throws IOException {
        long sittingRecordId = 9L;
        String requestJson = Resources.toString(getResource("singleSittingRecordWithPotentialDuplicate.json"), UTF_8);
        requestJson = requestJson.replace("<flag>", "true");
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                        .map(SittingRecordWrapper::new)
                        .peek(sittingRecordWrapper ->
                            sittingRecordWrapper.setSittingRecordId(sittingRecordId)
                        )
                        .toList();

        sittingRecordService.saveSittingRecords("SSC_ID",
                sittingRecordWrappers,
                recordSittingRecordRequest.getRecordedByName(),
                recordSittingRecordRequest.getRecordedByIdamId());

        verify(sittingRecordRepository).save(isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class));
    }

    @Test
    void shouldNotDeleteRecordSittingRecordsWhenPotentialDuplicateRecordButNoReplaceDuplicate() throws IOException {
        long sittingRecordId = 9L;
        String requestJson = Resources.toString(getResource("singleSittingRecordWithPotentialDuplicate.json"), UTF_8);
        requestJson = requestJson.replace("<flag>", "false");
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                        .map(SittingRecordWrapper::new)
                        .peek(sittingRecordWrapper -> {
                            sittingRecordWrapper.setSittingRecordId(sittingRecordId);
                            sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                        })
                        .toList();

        sittingRecordService.saveSittingRecords("SSC_ID",
                sittingRecordWrappers,
                recordSittingRecordRequest.getRecordedByName(),
                recordSittingRecordRequest.getRecordedByIdamId());

        verify(sittingRecordRepository).save(isA(uk.gov.hmcts.reform.jps.domain.SittingRecord.class));
    }
}
