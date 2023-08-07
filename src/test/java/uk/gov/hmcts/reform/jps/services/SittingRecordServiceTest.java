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
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@ExtendWith(MockitoExtension.class)
class SittingRecordServiceTest {

    @Mock
    private SittingRecordRepository sittingRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SittingRecordService sittingRecordService;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecordArgumentCaptor;

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

        assertThat(sittingRecords).hasSize(2);

        assertEquals(sittingRecords.get(0), getDomainSittingRecords(2).get(0));
        assertEquals(sittingRecords.get(1), getDomainSittingRecords(2).get(1));
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

        assertThat(sittingRecords).hasSize(1);
        assertEquals(sittingRecords.get(0), domainSittingRecords.get(0));
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

        assertThat(sittingRecords).hasSize(1);
        assertEquals(sittingRecords.get(0), domainSittingRecords.get(0));
    }


    @Test
    void shouldSaveSittingRecordsWhenRequestIsValid() throws IOException {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );
        sittingRecordService.saveSittingRecords("test",
                                                recordSittingRecordRequest);
        verify(sittingRecordRepository, times(3))
            .save(sittingRecordArgumentCaptor.capture());

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords = sittingRecordArgumentCaptor.getAllValues();
        assertThat(sittingRecords).extracting(SittingRecord_.SITTING_DATE, SittingRecord_.STATUS_ID,
                                              SittingRecord_.EPIMMS_ID, SittingRecord_.HMCTS_SERVICE_ID,
                                              SittingRecord_.PERSONAL_CODE, SittingRecord_.CONTRACT_TYPE_ID,
                                              SittingRecord_.JUDGE_ROLE_TYPE_ID, SittingRecord_.AM, SittingRecord_.PM)
                .contains(
                    tuple(of(2023, Month.MAY, 11), StatusId.RECORDED.name(), "852649",
                          "test", "4918178", 1L, "Judge", false, true),
                    tuple(of(2023, Month.APRIL, 10), StatusId.RECORDED.name(), "852649",
                          "test", "4918179", 1L, "Judge", true, false),
                    tuple(of(2023, Month.MARCH, 9), StatusId.RECORDED.name(), "852649",
                          "test", "4918180", 1L, "Judge", true, true)
        );

        assertThat(sittingRecords).flatExtracting(uk.gov.hmcts.reform.jps.domain.SittingRecord::getStatusHistories)
            .extracting("statusId", "changedByUserId", "changedByName")
            .contains(
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder"),
                tuple("RECORDED", "d139a314-eb40-45f4-9e7a-9e13f143cc3a", "Recorder")
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
                .statusId(StatusId.RECORDED.name())
                .regionId("1")
                .epimmsId("epims001")
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
                    .statusId(StatusId.RECORDED.name())
                    .regionId("1")
                    .epimmsId("epims001")
                    .hmctsServiceId("sscs")
                    .personalCode("001")
                    .contractTypeId(count)
                    .judgeRoleTypeId("HighCourt")
                    .am(AM.name())
                    .pm(PM.name())
                    .build())
            .collect(Collectors.toList());
    }

}
