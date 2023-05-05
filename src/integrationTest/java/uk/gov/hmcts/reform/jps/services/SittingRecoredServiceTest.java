package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SittingRecoredServiceTest {
    @Autowired
    private SittingRecordRepository recordRepository;

    private SittingRecordService sittingRecordService;
    private static final String USER_ID = "718f61d9-4fa1-4734-8683-94bb6a6c4f29";

    @BeforeEach
    void beforeEach() {
        sittingRecordService = new SittingRecordService(recordRepository);
    }

    @Test
    void shouldReturnQueriedRecordsWithMandatoryFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2, USER_ID);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimsId(sittingRecord.getEpimsId())
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId(),
            USER_ID
        );

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected =
            uk.gov.hmcts.reform.jps.model.out.SittingRecord.builder()
            .sittingRecordId(persistedSittingRecord.getId())
            .sittingDate(persistedSittingRecord.getSittingDate())
            .statusId(persistedSittingRecord.getStatusId())
            .regionId(persistedSittingRecord.getRegionId())
            .epimsId(persistedSittingRecord.getEpimsId())
            .hmctsServiceId(persistedSittingRecord.getHmctsServiceId())
            .personalCode(persistedSittingRecord.getPersonalCode())
            .contractTypeId(persistedSittingRecord.getContractTypeId())
            .judgeRoleTypeId(persistedSittingRecord.getJudgeRoleTypeId())
            .am(persistedSittingRecord.isAm() ? AM.name() : null)
            .pm(persistedSittingRecord.isPm() ? PM.name() : null)
            .createdDateTime(persistedSittingRecord.getCreatedDateTime())
            .createdByUserId(persistedSittingRecord.getCreatedByUserId())
            .changeDateTime(persistedSittingRecord.getChangeDateTime())
            .changeByUserId(persistedSittingRecord.getChangeByUserId())
            .build();

        assertThat(response).hasSize(1);
        assertThat(expected).isEqualTo(response.get(0));
    }

    private  SittingRecord getSittingRecord(long contractTypeId, String userId) {
        return SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(StatusId.RECORDED.name())
            .regionId("1")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(contractTypeId)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .duration("10")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId(userId)
            .build();
    }


    @Test
    void shouldReturnQueriedRecordsWithAllSearchFieldsSet() {
        SittingRecord sittingRecord = getSittingRecord(2, USER_ID);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(10)
            .offset(0)
            .regionId(sittingRecord.getRegionId())
            .epimsId(sittingRecord.getEpimsId())
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .personalCode("001")
            .judgeRoleTypeId("HighCourt")
            .createdByUserId(USER_ID)
            .statusId(StatusId.RECORDED)
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            sittingRecord.getHmctsServiceId(),
            USER_ID
        );

        uk.gov.hmcts.reform.jps.model.out.SittingRecord expected =
            uk.gov.hmcts.reform.jps.model.out.SittingRecord.builder()
            .sittingRecordId(persistedSittingRecord.getId())
            .sittingDate(persistedSittingRecord.getSittingDate())
            .statusId(persistedSittingRecord.getStatusId())
            .regionId(persistedSittingRecord.getRegionId())
            .epimsId(persistedSittingRecord.getEpimsId())
            .hmctsServiceId(persistedSittingRecord.getHmctsServiceId())
            .personalCode(persistedSittingRecord.getPersonalCode())
            .contractTypeId(persistedSittingRecord.getContractTypeId())
            .judgeRoleTypeId(persistedSittingRecord.getJudgeRoleTypeId())
            .am(persistedSittingRecord.isAm() ? AM.name() : null)
            .pm(persistedSittingRecord.isPm() ? PM.name() : null)
            .createdDateTime(persistedSittingRecord.getCreatedDateTime())
            .createdByUserId(persistedSittingRecord.getCreatedByUserId())
            .changeDateTime(persistedSittingRecord.getChangeDateTime())
            .changeByUserId(persistedSittingRecord.getChangeByUserId())
            .build();

        assertThat(response).hasSize(1);
        assertThat(expected).isEqualTo(response.get(0));
    }

    private void createMultipleRecords(int count, Function<Long, String> evaluateUser) {
        for (long i = 1; i <= count; i++) {
            SittingRecord sittingRecord = getSittingRecord(i, evaluateUser.apply(i));
            SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
            assertThat(persistedSittingRecord).isNotNull();
        }
    }

    @Test
    void shouldReturnLoggedInUserRecordsFollowedByOtherRecordsWhenUserRecordsIsInsufficient() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";
        String otherUser = UUID.randomUUID().toString();

        createMultipleRecords(25, counter -> counter < 13 ? USER_ID : otherUser);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            hmctsServiceId,
            USER_ID
        );

        assertThat(response).hasSize(5);

        assertThat(response)
            .extracting("contractTypeId", "createdByUserId")
            .contains(
                tuple(11L, USER_ID),
                tuple(12L, USER_ID),
                tuple(13L, otherUser),
                tuple(14L, otherUser),
                tuple(15L, otherUser)
            );
    }

    @Test
    void shouldReturnRecordsFollowedByOtherRecordsWhenNoLoggedInUserRecordsPresent() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";
        String otherUser = UUID.randomUUID().toString();

        createMultipleRecords(25, counter -> otherUser);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            hmctsServiceId,
            USER_ID
        );


        assertThat(response).hasSize(5);

        assertThat(response)
            .extracting("contractTypeId", "createdByUserId")
            .contains(
                tuple(11L, otherUser),
                tuple(12L, otherUser),
                tuple(13L, otherUser),
                tuple(14L, otherUser),
                tuple(15L, otherUser)
            );
    }


    @Test
    void shouldReturnOffsetOfRecordsWhenPageAndOffsetProvidedAlongWithCreatedByUserId() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";

        createMultipleRecords(25, counter -> USER_ID);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            hmctsServiceId,
            UUID.randomUUID().toString()
        );


        assertThat(response).hasSize(5);
        assertThat(response)
            .extracting("contractTypeId", "createdByUserId")
            .contains(
                tuple(11L, USER_ID),
                tuple(12L, USER_ID),
                tuple(13L, USER_ID),
                tuple(14L, USER_ID),
                tuple(15L, USER_ID)
            );
    }

    @Test
    void shouldReturnLastFewRecordsWhenRecordsAreLessThanPageSize() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";
        String otherUser = UUID.randomUUID().toString();

        createMultipleRecords(22, counter -> counter < 13 ? USER_ID : otherUser);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            hmctsServiceId,
            USER_ID
        );


        assertThat(response).hasSize(2);
        assertThat(response)
            .extracting("contractTypeId", "createdByUserId")
            .contains(
                tuple(21L, otherUser),
                tuple(22L, otherUser)
            );
    }

    @Test
    void shouldReturnLastFewRecordsWhenRecordsAreLessThanPageSizeAndSortOrderDecending() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";
        String otherUser = UUID.randomUUID().toString();

        createMultipleRecords(22, counter -> counter < 13 ? USER_ID : otherUser);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(20)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.DESCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        List<uk.gov.hmcts.reform.jps.model.out.SittingRecord> response = sittingRecordService.getSittingRecords(
            recordSearchRequest,
            hmctsServiceId,
            USER_ID
        );


        assertThat(response).hasSize(2);
        assertThat(response)
            .as("Extracting unique value by user")
            .extracting("contractTypeId", "createdByUserId")
            .contains(
                tuple(14L, otherUser),
                tuple(13L, otherUser)
            );
    }

    @Test
    void shouldReturnTotalRecordCounts() {
        String reasonId = "1";
        String epimsId = "123";
        String hmctsServiceId = "ssc_id";

        createMultipleRecords(25, counter -> USER_ID);

        SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .pageSize(5)
            .offset(10)
            .regionId(reasonId)
            .epimsId(epimsId)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(4))
            .dateRangeTo(LocalDate.now())
            .build();

        int totalRecordCount = sittingRecordService.getTotalRecordCount(
            recordSearchRequest,
            hmctsServiceId
        );

        assertThat(totalRecordCount)
            .isEqualTo(25);
    }
}
