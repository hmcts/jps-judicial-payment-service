package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@ExtendWith(MockitoExtension.class)
class SittingRecordServiceTest {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String UPDATED_BY_USER_ID = UUID.randomUUID().toString();
    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();


    @Mock
    private SittingRecordRepository sittingRecordRepository;

    @InjectMocks
    private SittingRecordService sittingRecordService;

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
            .hasSize(2);

        assertThat(sittingRecords)
            .isEqualTo(getDomainSittingRecords(2));
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
}
