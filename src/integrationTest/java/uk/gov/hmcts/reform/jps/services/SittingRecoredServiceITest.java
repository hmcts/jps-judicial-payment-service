package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.reform.jps.model.DateOrder.ASCENDING;
import static uk.gov.hmcts.reform.jps.model.DateOrder.DESCENDING;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SittingRecoredServiceITest {
    public static final String EPIM_ID = "123";
    public static final String SSC_ID = "ssc_id";
    public static final String CONTRACT_TYPE_ID = "contractTypeId";
    public static final String CREATED_BY_USER_ID = "createdByUserId";
    @Autowired
    private SittingRecordRepository recordRepository;

    private SittingRecordService sittingRecordService;
    private static final String USER_ID = UUID.randomUUID().toString();

    @BeforeEach
    void beforeEach() {
        sittingRecordService = new SittingRecordService(recordRepository);
    }

    @Test
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

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
            .epimsId(persistedSittingRecord.getEpimsId())
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

    private  SittingRecord getSittingRecord(long contractTypeId) {
        StatusId recorded = StatusId.RECORDED;
        SittingRecord.SittingRecordBuilder builder = SittingRecord.builder();
        return builder
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(recorded.name())
            .regionId("1")
            .epimsId(EPIM_ID)
            .hmctsServiceId(SSC_ID)
            .personalCode("001")
            .contractTypeId(contractTypeId)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId(USER_ID)
            .build();
    }


    @Test
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

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

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected = getSittingRecord(persistedSittingRecord);

        assertThat(response).hasSize(1);
        assertThat(expected).isEqualTo(response.get(0));
    }

    private void createMultipleRecords(int count) {
        for (long i = 1; i <= count; i++) {
            SittingRecord sittingRecord = getSittingRecord(i);
            SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
            assertThat(persistedSittingRecord).isNotNull();
        }
    }

    @Test
    void shouldReturnOffset10RecordsOnwardsInAscendingOrder() {
        String reasonId = "1";

        createMultipleRecords(25);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
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
        String reasonId = "1";

        createMultipleRecords(22);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(DESCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
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
                tuple(2L, USER_ID),
                tuple(1L, USER_ID)
            );
    }

    @Test
    void shouldReturnTotalRecordCounts() {
        String reasonId = "1";

        createMultipleRecords(25);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(EPIM_ID)
            .dateOrder(ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        int totalRecordCount = sittingRecordService.getTotalRecordCount(
            recordSearchRequest,
            SSC_ID
        );

        assertThat(totalRecordCount)
            .isEqualTo(25);
    }
}
