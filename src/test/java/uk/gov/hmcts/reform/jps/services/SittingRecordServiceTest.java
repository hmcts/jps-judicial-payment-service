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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.security.idam.IdamRepository;

import java.io.IOException;
import java.security.Security;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.time.LocalDate.of;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.*;

@ExtendWith(MockitoExtension.class)
class SittingRecordServiceTest {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String UPDATED_BY_USER_ID = UUID.randomUUID().toString();
    private static final LocalDateTime CURRENT_DATE_TIME = now();

    private static final Long ID = new Random().nextLong();

    @Mock
    private SittingRecordRepository sittingRecordRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    UserInfo userInfo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SittingRecordService sittingRecordService;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecordArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        assertThat(sittingRecords)
            .hasSize(2)
            .isEqualTo(getDomainSittingRecords(2));
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

        assertThat(sittingRecords)
            .hasSize(1)
            .isEqualTo(domainSittingRecords);
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

        assertThat(sittingRecords)
            .hasSize(1)
            .isEqualTo(domainSittingRecords);
    }

    private List<uk.gov.hmcts.reform.jps.domain.SittingRecord> getDbSittingRecords(int limit) {
        return LongStream.range(1, limit + 1)
            .mapToObj(count -> uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(count)
                .sittingDate(LocalDate.now().minusDays(2))
                .statusId(StatusId.RECORDED.name())
                .regionId("1")
                .epimsId("epims001")
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
                .statusId(StatusId.RECORDED.name())
                .regionId("1")
                .epimsId("epims001")
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
        sittingRecordService.saveSittingRecords(
            "test",
            recordSittingRecordRequest
        );
        verify(sittingRecordRepository, times(3))
            .save(sittingRecordArgumentCaptor.capture());

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords = sittingRecordArgumentCaptor.getAllValues();
        assertThat(sittingRecords).extracting("sittingDate", "statusId", "epimsId", "hmctsServiceId",
                                              "personalCode", "contractTypeId", "judgeRoleTypeId", "am", "pm"
            )
            .contains(
                tuple(of(2023, Month.MAY, 11), "RECORDED", "852649", "test", "4918178", 1L, "Judge", false, true),
                tuple(of(2023, Month.APRIL, 10), "RECORDED", "852649", "test", "4918178", 1L, "Judge", true, false),
                tuple(of(2023, Month.MARCH, 9), "RECORDED", "852649", "test", "4918178", 1L, "Judge", true, true)
            );

        assertThat(sittingRecords).flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .extracting("statusId", "changeByUserId", "changeByName")
            .contains(
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder")
            );

        assertThat(sittingRecords).describedAs("Created date assertion")
            .flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .allMatch(m -> LocalDateTime.now().minusMinutes(5).isBefore(m.getChangeDateTime()));
    }


    private uk.gov.hmcts.reform.jps.domain.SittingRecord deleteTestSetUp(String changeById, String state) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(RECORDED.name())
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(changeById)
            .changeByName("John Smith")
            .build();

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
            .id(ID)
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(state)
            .regionId("1")
            .epimsId("epims001")
            .hmctsServiceId("sscs")
            .personalCode("001")
            .contractTypeId(1L)
            .judgeRoleTypeId("HighCourt")
            .am(true)
            .pm(true)
            .createdByUserId(USER_ID)
            .createdDateTime(CURRENT_DATE_TIME.minusDays(2))
            .changeByUserId(changeById)
            .changeDateTime(CURRENT_DATE_TIME.minusDays(1))
            .build();

        sittingRecord.addStatusHistory(statusHistory);

        return sittingRecord;
    }

    @Test
    void shouldDeleteRecordRecorder() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, RECORDED.name());

        when(sittingRecordRepository.findById(sittingRecord.getId())).thenReturn(Optional.of(sittingRecord));

        System.out.print("post set up");
        System.out.print(sittingRecord.getStatusId());
        System.out.print(sittingRecord.getId().toString());

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory = sittingRecord.getStatusHistories().stream().max(Comparator.comparing(
            StatusHistory::getChangeDateTime));
        StatusHistory statusHistory = null;
        if (optionalStatusHistory != null && !optionalStatusHistory.isEmpty()) {
            statusHistory = optionalStatusHistory.get();
        }

        assertThat(statusHistory.getStatusId().equals("DELETED"));

    }

    @Test
    void shouldDeleteRecordSubmitter() {

        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED.name());

        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory = sittingRecord.getLatestStatusHistory();
        StatusHistory statusHistory = null;
        if (optionalStatusHistory != null && !optionalStatusHistory.isEmpty()) {
            statusHistory = optionalStatusHistory.get();
        }

        assertThat(statusHistory.getStatusId().equals("DELETED"));

    }

    @Test
    void shouldDeleteRecordAdmin() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, SUBMITTED.name());


        sittingRecordService.deleteSittingRecord(sittingRecord.getId());

        Optional<StatusHistory> optionalStatusHistory = sittingRecord.getLatestStatusHistory();
        StatusHistory statusHistory = null;
        if (optionalStatusHistory != null && !optionalStatusHistory.isEmpty()) {
            statusHistory = optionalStatusHistory.get();
        }

        assertThat(statusHistory.getStatusId().equals("DELETED"));

    }

    @Test
    void differentRecorderID() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED.name());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });

        Optional<StatusHistory> optionalStatusHistory = sittingRecord.getLatestStatusHistory();
        StatusHistory statusHistory = null;
        if (optionalStatusHistory != null && !optionalStatusHistory.isEmpty()) {
            statusHistory = optionalStatusHistory.get();
        }

        assertThat(statusHistory.getStatusId().equals("RECORDED"));
        assertEquals("Sitting Record ID Not Found", exception.getMessage());
    }


    @Test
    void incorrectIDAMRole() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-publisher")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED.name());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
        assertEquals("User IDAM ID does not match the oldest Changed by IDAM ID ", exception.getMessage());
    }


    @Test
    void wrongStateRecorder() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-recorder")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(USER_ID, SUBMITTED.name());

        Exception exception = assertThrows(ConflictException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
        assertEquals("Sitting Record Status ID is in wrong state", exception.getMessage());
    }

    @Test
    void wrongStateSubmitter() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-submitter")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, SUBMITTED.name());

        Exception exception = assertThrows(ConflictException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
        assertEquals("Sitting Record Status ID is in wrong state", exception.getMessage());
    }

    @Test
    void wrongStateAdmin() {
        UserInfo userInfo =  UserInfo.builder().roles(List.of("jps-admin")).uid(USER_ID).build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord = deleteTestSetUp(UPDATED_BY_USER_ID, RECORDED.name());

        Exception exception = assertThrows(ConflictException.class, () -> {
            sittingRecordService.deleteSittingRecord(sittingRecord.getId());
        });
        assertEquals("Sitting Record Status ID is in wrong state", exception.getMessage());
    }

}
