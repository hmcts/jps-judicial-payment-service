package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.model.FinancialYearRecords;
import uk.gov.hmcts.reform.jps.model.PublishSittingRecordCount;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class PublishSittingRecordServiceTest {
    public static final String PERSONAL_CODE = "4918178";

    public static final String HMCTS_SERVICE_CODE = "BBA3";
    public static final String MEDICAL_STAFF = "44";
    public static final BigDecimal HIGHER_THRESHOLD_FEE = new BigDecimal(100L);
    public static final BigDecimal STANDARD_FEE = new BigDecimal(10);
    public static final BigDecimal LONDON_WEIGHTED_FEE = new BigDecimal(250);
    @Mock
    private SittingDaysService sittingDaysService;
    @Mock
    private SittingRecordRepository sittingRecordRepository;
    @Mock
    private FeeService feeService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private JudicialOfficeHolderService judicialOfficeHolderService;

    @InjectMocks
    private PublishSittingRecordService publishSittingRecordService;

    @Test
    void shouldReturnZeroPublishRecordCountWhenNoRecordPresent() {
        LocalDate localDate = of(2023, 10, 19);
        try (MockedStatic<LocalDate> localDateMockedStatic = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);

            PublishSittingRecordCount publishSittingRecordCount = publishSittingRecordService
                .retrievePublishedRecords("4918178");
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
    void shouldReturnPublishRecordCountWhenRecordPresent() {
        when(sittingDaysService.getSittingCount(eq(PERSONAL_CODE), anyString()))
            .thenReturn(2L);
        when(sittingRecordRepository.findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
            eq(PERSONAL_CODE),
            eq(SUBMITTED),
            any(LocalDate.class),
            any(LocalDate.class)
        )).thenReturn(300L);

        LocalDate localDate = of(2023, 10, 19);
        try (MockedStatic<LocalDate> localDateMockedStatic = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            PublishSittingRecordCount publishSittingRecordCount = publishSittingRecordService
                .retrievePublishedRecords(PERSONAL_CODE);

            assertThat(publishSittingRecordCount).isEqualTo(
                PublishSittingRecordCount.builder()
                    .currentFinancialYear(FinancialYearRecords.builder()
                                              .submittedCount(300)
                                              .publishedCount(2)
                                              .financialYear("2023-24")
                                              .build())
                    .previousFinancialYear(FinancialYearRecords.builder()
                                               .submittedCount(300)
                                               .publishedCount(2)
                                               .financialYear("2022-23")
                                               .build())
                    .build());
        }
    }

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
      # HigherMedicalRateSession, MedicalThreshold
        false,                      0
        true,                       10
        """)
    void shouldReturnHigherThresholdFeeWhenMedicalMember(
        boolean higherMedicalRateSession,
        int medicalThreshold
    ) {

        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            HMCTS_SERVICE_CODE,
            MEDICAL_STAFF,
            LocalDate.now()))
            .thenReturn(Fee.builder()
                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                            .build());

        when(applicationProperties.isMedicalMember(MEDICAL_STAFF))
            .thenReturn(true);

        when(applicationProperties.getMedicalThreshold())
            .thenReturn(medicalThreshold);

        BigDecimal fee = publishSittingRecordService.calculateJohFee(
            HMCTS_SERVICE_CODE,
            PERSONAL_CODE,
            MEDICAL_STAFF,
            LocalDate.now(),
            higherMedicalRateSession
        );

        assertThat(fee).isEqualTo(HIGHER_THRESHOLD_FEE);
    }

    @Test
    void shouldReturnStandardFeeWhenMedicalMember() {
        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            HMCTS_SERVICE_CODE,
            MEDICAL_STAFF,
            LocalDate.now()))
            .thenReturn(Fee.builder()
                            .standardFee(STANDARD_FEE)
                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                            .build());

        when(applicationProperties.isMedicalMember(MEDICAL_STAFF))
            .thenReturn(true);

        when(applicationProperties.getMedicalThreshold())
            .thenReturn(40);


        BigDecimal fee = publishSittingRecordService.calculateJohFee(
            HMCTS_SERVICE_CODE,
            PERSONAL_CODE,
            MEDICAL_STAFF,
            LocalDate.now(),
            false
        );

        assertThat(fee).isEqualTo(STANDARD_FEE);

    }


    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
      # LondonFlag,   Fee,  FeeType
        true,          250, London Fee
        false,         10,  Standard Fee
        """)
    void shouldReturnFeeWhenNonMedicalMember(
        boolean londonFlag,
        int expectedFee,
        String feeType) {
        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            HMCTS_SERVICE_CODE,
            MEDICAL_STAFF,
            LocalDate.now()))
            .thenReturn(Fee.builder()
                            .standardFee(STANDARD_FEE)
                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                            .londonWeightedFee(LONDON_WEIGHTED_FEE)
                            .build());

        when(applicationProperties.isMedicalMember(MEDICAL_STAFF))
            .thenReturn(false);

        when(judicialOfficeHolderService.getLondonFlag(PERSONAL_CODE, LocalDate.now()))
            .thenReturn(Optional.of(londonFlag));

        BigDecimal fee = publishSittingRecordService.calculateJohFee(
            HMCTS_SERVICE_CODE,
            PERSONAL_CODE,
            MEDICAL_STAFF,
            LocalDate.now(),
            false
        );

        assertThat(fee)
            .as(feeType)
            .isEqualTo(new BigDecimal(expectedFee));
    }

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
      # LondonFlag
        true
        false
        """)
    void shouldReturnStandardFeeWhenNonMedicalMemberWithLondonWeightedFeeNotPresent(boolean londonFlag) {
        when(judicialOfficeHolderService.getLondonFlag(PERSONAL_CODE, LocalDate.now()))
            .thenReturn(Optional.of(londonFlag));
        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            HMCTS_SERVICE_CODE,
            MEDICAL_STAFF,
            LocalDate.now()))
            .thenReturn(Fee.builder()
                            .standardFee(STANDARD_FEE)
                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                            .build());

        BigDecimal fee = publishSittingRecordService.calculateJohFee(
            HMCTS_SERVICE_CODE,
            PERSONAL_CODE,
            MEDICAL_STAFF,
            LocalDate.now(),
            false
        );

        assertThat(fee).isEqualTo(STANDARD_FEE);
    }

    @Test
    void shouldReturnHigherFeeWhenSittingDateIsInPreviousFinancialYear() {
        LocalDate localDate = of(2023, 10, 19);
        try (MockedStatic<LocalDate> localDateMockedStatic = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            LocalDate sittingDate = LocalDate.now().minusYears(1);
            when(sittingDaysService.getSittingCount(eq(PERSONAL_CODE), anyString()))
                .thenReturn(2L);
            when(sittingRecordRepository.findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
                eq(PERSONAL_CODE),
                eq(SUBMITTED),
                any(LocalDate.class),
                any(LocalDate.class)
            )).thenReturn(300L);
            when(applicationProperties.isMedicalMember(MEDICAL_STAFF))
                .thenReturn(true);

            when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
                HMCTS_SERVICE_CODE,
                MEDICAL_STAFF,
                sittingDate))
                .thenReturn(Fee.builder()
                                .standardFee(STANDARD_FEE)
                                .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                .build());

            BigDecimal fee = publishSittingRecordService.calculateJohFee(
                HMCTS_SERVICE_CODE,
                PERSONAL_CODE,
                MEDICAL_STAFF,
                sittingDate,
                false
            );
            assertThat(fee).isEqualTo(HIGHER_THRESHOLD_FEE);
        }
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSittingDateIsOutOfRange() {
        LocalDate localDate = of(2023, 10, 19);
        try (MockedStatic<LocalDate> localDateMockedStatic = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            LocalDate sittingDate = LocalDate.now().minusYears(2);
            when(sittingDaysService.getSittingCount(eq(PERSONAL_CODE), anyString()))
                .thenReturn(2L);
            when(sittingRecordRepository.findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
                eq(PERSONAL_CODE),
                eq(SUBMITTED),
                any(LocalDate.class),
                any(LocalDate.class)
            )).thenReturn(300L);
            when(applicationProperties.isMedicalMember(MEDICAL_STAFF))
                .thenReturn(true);

            when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
                HMCTS_SERVICE_CODE,
                MEDICAL_STAFF,
                sittingDate
                ))
                .thenReturn(Fee.builder()
                                .standardFee(STANDARD_FEE)
                                .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                .build());

            assertThatThrownBy(() -> publishSittingRecordService.calculateJohFee(
                HMCTS_SERVICE_CODE,
                PERSONAL_CODE,
                MEDICAL_STAFF,
                sittingDate,
                false
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Financial year is invalid : 2021-22");
        }
    }
}
