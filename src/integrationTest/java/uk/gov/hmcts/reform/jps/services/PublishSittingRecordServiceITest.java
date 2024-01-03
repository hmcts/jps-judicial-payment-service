package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.model.FinancialYearRecords;
import uk.gov.hmcts.reform.jps.model.PublishSittingRecordCount;
import uk.gov.hmcts.reform.jps.model.out.PublishResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@Transactional
class PublishSittingRecordServiceITest extends BaseTest {

    @Autowired
    private PublishSittingRecordService publishSittingRecordService;

    private static final String PC_4918178 = "4918178";

    @Test
    @Sql(RESET_DATABASE)
    void shouldReturnZeroPublishRecordCountWhenNoRecordPresent() {
        LocalDate localDate = of(2023, 10, 19);
        try (MockedStatic<LocalDate> localDateMockedStatic = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            PublishSittingRecordCount publishSittingRecordCount = publishSittingRecordService.retrievePublishedRecords(
                PC_4918178);
            assertThat(publishSittingRecordCount).isEqualTo(
                PublishSittingRecordCount.builder()
                    .currentFinancialYear(FinancialYearRecords.builder()
                                              .financialYear("2023-24")
                                              .build())
                    .previousFinancialYear(FinancialYearRecords.builder()
                                               .financialYear("2022-23")
                                               .build())
                    .build());
        }
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA})
    void shouldReturnPublishRecordCountWhenRecordPresent() {
        LocalDate localDate = of(2023, 10, 10);
        String personalCode = PC_4918178;
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            Mockito.CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            PublishSittingRecordCount publishSittingRecordCount = publishSittingRecordService
                .retrievePublishedRecords(personalCode);

            assertThat(publishSittingRecordCount).isEqualTo(
                PublishSittingRecordCount.builder()
                    .currentFinancialYear(FinancialYearRecords.builder()
                                              .submittedCount(1)
                                              .publishedCount(300)
                                              .financialYear("2023-24")
                                              .build())
                    .previousFinancialYear(FinancialYearRecords.builder()
                                               .submittedCount(2)
                                               .publishedCount(3)
                                               .financialYear("2022-23")
                                               .build())
                    .build());
        }
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA, INSERT_FEE})
    void shouldReturnHigherThresholdFeeWhenMedicalMember() {
        LocalDate localDate = of(2023, 10, 10);
        String personalCode = PC_4918178;
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            Mockito.CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            BigDecimal fee = publishSittingRecordService
                .calculateJohFee(
                    "BBA3",
                    personalCode,
                    "58",
                    now(),
                    false
                );

            assertThat(fee).isEqualTo(new BigDecimal(100));
        }
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA, INSERT_FEE})
    void shouldReturnStandardFeeWhenMedicalMember() {
        LocalDate localDate = of(2023, 10, 10);
        String personalCode = "5918178";
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            Mockito.CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            BigDecimal fee = publishSittingRecordService
                .calculateJohFee(
                    "BBA3",
                    personalCode,
                    "58",
                    now(),
                    false
                );

            assertThat(fee).isEqualTo(new BigDecimal(10));
        }
    }

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
        # PersonalCode,  JudgeRoleType, Fee, FeeType
          5123421,       101,           250, London Fee
          4918178,       100,           20,  Standard Fee
          """)
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA, INSERT_FEE, INSERT_JOH})
    void shouldReturnFeeWhenNonMedicalMember(
        String personalCode,
        String judgeRoleTypeId,
        int expectedFee,
        String feeType) {
        LocalDate localDate = of(2023, 10, 10);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            Mockito.CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            BigDecimal fee = publishSittingRecordService
                .calculateJohFee(
                    "ABA5",
                    personalCode,
                    judgeRoleTypeId,
                    now(),
                    false
                );
            assertThat(fee)
                .as(feeType)
                .isEqualTo(new BigDecimal(expectedFee));
        }
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA, INSERT_FEE, INSERT_JOH})
    void shouldFeeWhenNonMedicalMember() {
        LocalDate localDate = of(2023, 10, 10);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            Mockito.CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            PublishResponse publishResponse = publishSittingRecordService
                .publishRecords(
                    "ABA5",
                    now(),
                    "publishedByIdAMId",
                    "publishedByName",
                    false
                );

            System.out.println(publishResponse);
        }
    }

}
