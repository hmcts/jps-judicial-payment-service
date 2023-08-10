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
import uk.gov.hmcts.reform.jps.components.BaseEvaluateDuplicate;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.RecordSubmitFields;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SubmitSittingRecordResponse;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.io.IOException;
import java.time.LocalDate;
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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.CLOSED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SittingRecordServiceTest extends BaseEvaluateDuplicate {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String UPDATED_BY_USER_ID = UUID.randomUUID().toString();

    private static final Long ID = new Random().nextLong();

    @Mock
    private SittingRecordRepository sittingRecordRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private DuplicateCheckerService duplicateCheckerService;

    @Mock
    private StatusHistoryService statusHistoryService;

    @Mock
    private JudicialOfficeHolderService judicialOfficeHolderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SittingRecordService sittingRecordService;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecordArgumentCaptor;
    @Captor
    private ArgumentCaptor<Long> records;
    @Captor
    private ArgumentCaptor<String> data;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldReturnTotalRecordCount() {
        when(sittingRecordRepository.totalRecords(
            isA(SittingRecordSearchRequest.class),
            isA(String.class)
        ))
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
        when(sittingRecordRepository.find(
            isA(SittingRecordSearchRequest.class),
            isA(String.class)
        ))
            .thenReturn(getDbSittingRecords(2));

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        assertThat(sittingRecords).hasSize(2);


        assertThat(sittingRecords.get(0)).isEqualTo(getDomainSittingRecords(2).get(0));
        assertThat(sittingRecords.get(1)).isEqualTo(getDomainSittingRecords(2).get(1));
    }


    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDbWithAmNull() {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = getDbSittingRecords(1);
        dbSittingRecords.get(0).setAm(false);

        when(sittingRecordRepository.find(
            isA(SittingRecordSearchRequest.class),
            isA(String.class)
        ))
            .thenReturn(dbSittingRecords);

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        List<SittingRecord> domainSittingRecords = getDomainSittingRecords(1);
        domainSittingRecords.get(0).setAm(null);

        assertThat(sittingRecords).hasSize(1);
        assertThat(sittingRecords.get(0)).isEqualTo(domainSittingRecords.get(0));
    }

    @Test
    void shouldReturnSittingRecordsWhenRecordPresentInDbWithPmNull() {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = getDbSittingRecords(1);
        dbSittingRecords.get(0).setPm(false);

        when(sittingRecordRepository.find(
            isA(SittingRecordSearchRequest.class),
            isA(String.class)
        ))
            .thenReturn(dbSittingRecords);

        List<SittingRecord> sittingRecords = sittingRecordService.getSittingRecords(
            SittingRecordSearchRequest.builder().build(),
            "test"
        );

        List<SittingRecord> domainSittingRecords = getDomainSittingRecords(1);
        domainSittingRecords.get(0).setPm(null);

        assertThat(sittingRecords).hasSize(1);
        assertThat(sittingRecords.get(0)).isEqualTo(domainSittingRecords.get(0));
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

        sittingRecordService.saveSittingRecords("test",
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
                    tuple(of(2023, Month.MAY, 11), RECORDED, "852649", "test", "4918500", 1L, "Judge", false, true),
                    tuple(of(2023, Month.APRIL, 10), RECORDED, "852649", "test", "4918179", 1L, "Judge", true, false),
                    tuple(of(2023, Month.MARCH, 9), RECORDED, "852649", "test", "4918180", 1L, "Judge", true, true)
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
            .allMatch(m -> now().minusMinutes(5).isBefore(m.getChangeDateTime()));
    }

    private List<uk.gov.hmcts.reform.jps.domain.SittingRecord> getDbSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(count)
                .sittingDate(LocalDate.now().minusDays(2))
                .statusId(RECORDED)
                .regionId("1")
                .epimmsId("epimms001")
                .hmctsServiceId("sscs")
                .personalCode("001")
                .contractTypeId(count)
                .judgeRoleTypeId("HighCourt")
                .am(true)
                .pm(true)
                .build())
            .collect(Collectors.toList());
    }

    private List<SittingRecord> getDomainSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> SittingRecord.builder()
                    .sittingRecordId(count)
                    .sittingDate(LocalDate.now().minusDays(2))
                    .statusId(StatusId.RECORDED)
                    .regionId("1")
                    .epimmsId("epimms001")
                    .hmctsServiceId("sscs")
                    .personalCode("001")
                    .contractTypeId(count)
                    .judgeRoleTypeId("HighCourt")
                    .am(AM.name())
                    .pm(PM.name())
                    .build())
            .collect(Collectors.toList());
    }


    private uk.gov.hmcts.reform.jps.domain.SittingRecord deleteTestSetUp(String changeById, StatusId state) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(state)
            .changeDateTime(now())
            .changeByUserId(changeById)
            .changeByName("John Smith")
            .build();

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
            .id(ID)
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(state)
            .regionId("1")
            .epimmsId("epimms001")
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

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        System.out.print("post set up");
        System.out.print(sittingRecord.getStatusId());
        System.out.print(sittingRecord.getId().toString());

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory
            = sittingRecord.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangeDateTime));
        StatusHistory statusHistory = null;
        if (optionalStatusHistory != null && !optionalStatusHistory.isEmpty()) {
            statusHistory = optionalStatusHistory.get();
        }

        assertThat(statusHistory.getStatusId().equals("DELETED"));

    }

    @Test
    void shouldDeleteRecordSubmitter() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(
            UPDATED_BY_USER_ID,
            RECORDED
        );

        StatusHistory statusHistory = sittingRecord.getLatestStatusHistory();
        assertThat(statusHistory).isNotNull();
        assertThat(statusHistory.getStatusId().equals("DELETED"));
    }

    @Test
    void shouldDeleteRecordAdmin() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(
            UPDATED_BY_USER_ID,
            SUBMITTED
        );

        StatusHistory statusHistory = sittingRecord.getLatestStatusHistory();
        assertThat(statusHistory).isNotNull();
        assertThat(statusHistory.getStatusId().equals("DELETED"));
    }

    @Test
    void differentRecorderID() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
            deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED);

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
            sittingRecordService.deleteSittingRecord(sittingRecord.getId())
        );

        StatusHistory statusHistory = sittingRecord.getLatestStatusHistory();
        assertThat(statusHistory).isNotNull();

        assertThat(statusHistory.getStatusId()).isEqualTo(RECORDED);
        assertThat(exception.getMessage())
            .isEqualTo("User IDAM ID does not match the oldest Changed by IDAM ID ");
    }

    @Test
    void wrongStateRecorder() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, SUBMITTED);

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        Exception exception = assertThrows(ConflictException.class, () ->
            sittingRecordService.deleteSittingRecord(sittingRecord.getId())
        );
        assertThat(exception.getMessage())
            .isEqualTo("Sitting Record Status ID is in wrong state");
    }

    @Test
    void wrongStateSubmitter() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, SUBMITTED);

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        Exception exception = assertThrows(ConflictException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
        assertThat(exception.getMessage())
            .isEqualTo("Sitting Record Status ID is in wrong state");
    }

    @Test
    void wrongStateAdmin() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, RECORDED);

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        Exception exception = assertThrows(ConflictException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
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

        when(sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            any(), any(), any(), any())
        ).thenReturn(Streamable.of(List.of(sittingRecordDuplicateCheckFields)));


        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        verify(duplicateCheckerService, times(3)).evaluate(any(), any());
    }

    @Test
    void shouldNotInvokeDuplicateCheckerWhenNoMatchingRecordsFoundInDb() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecordsPotentialDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordDuplicateCheckFields> dbSittingRecordDuplicateCheckFields = Collections.emptyList();

        when(sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            any(), any(), any(), any())
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
        String hmctsServiceCode = "BBA3";
        List<RecordSubmitFields> sittingRecordIds = List.of(
            getRecordSubmitFields(1L, 6L, "123"),
            getRecordSubmitFields(100L, 2L, "243"),
            getRecordSubmitFields(200L, 3L, "567"),
            getRecordSubmitFields(300L, 6L, "789"),
            getRecordSubmitFields(400L, 6L, "999")
        );

        when(judicialOfficeHolderService.getCrownServiceFlag("123"))
            .thenReturn(Optional.of(true));
        when(judicialOfficeHolderService.getCrownServiceFlag("789"))
            .thenReturn(Optional.of(false));
        when(judicialOfficeHolderService.getCrownServiceFlag("999"))
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
            hmctsServiceCode
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
            .getCrownServiceFlag(data.capture());

        assertThat(data.getAllValues())
            .containsExactlyInAnyOrder("123",
                                       "789",
                                       "999",
                                       "789",
                                       "999");
    }

    private static RecordSubmitFields getRecordSubmitFields(long id, long contractTypeId, String number) {
        return RecordSubmitFields.builder()
            .id(id)
            .contractTypeId(contractTypeId)
            .personalCode(number)
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
            hmctsServiceCode
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
}
