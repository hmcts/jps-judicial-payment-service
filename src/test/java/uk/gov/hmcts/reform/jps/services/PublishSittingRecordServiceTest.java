package uk.gov.hmcts.reform.jps.services;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Streamable;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.components.BasePublishSittingRecord;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.domain.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.CourtVenueInError;
import uk.gov.hmcts.reform.jps.model.FeeInError;
import uk.gov.hmcts.reform.jps.model.FileInfo;
import uk.gov.hmcts.reform.jps.model.FinancialYearRecords;
import uk.gov.hmcts.reform.jps.model.JohAttributesInError;
import uk.gov.hmcts.reform.jps.model.JohPayrollInError;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.model.PublishSittingRecordCount;
import uk.gov.hmcts.reform.jps.model.out.PublishResponse;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class PublishSittingRecordServiceTest extends BasePublishSittingRecord {
    public static final String PERSONAL_CODE = "4918178";

    public static final String MEDICAL_STAFF = "44";
    public static final BigDecimal HIGHER_THRESHOLD_FEE = new BigDecimal(100L);
    public static final BigDecimal STANDARD_FEE = new BigDecimal(10);
    public static final BigDecimal LONDON_WEIGHTED_FEE = new BigDecimal(250);
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String SERVICE_NAME = "SSCS";
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

    @Mock
    private ServiceService serviceService;

    @Mock
    private PublishErrorCheckerService publishErrorCheckerService;

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
            LocalDate.now()
        ))
            .thenReturn(Optional.of(Fee.builder()
                                        .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                        .build())
            );

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
            LocalDate.now()
        ))
            .thenReturn(Optional.of(Fee.builder()
                                        .standardFee(STANDARD_FEE)
                                        .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                        .build())
            );

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
            LocalDate.now()
        ))
            .thenReturn(Optional.of(Fee.builder()
                                        .standardFee(STANDARD_FEE)
                                        .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                        .londonWeightedFee(LONDON_WEIGHTED_FEE)
                                        .build())
            );

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
            LocalDate.now()
        ))
            .thenReturn(Optional.of(Fee.builder()
                                        .standardFee(STANDARD_FEE)
                                        .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                        .build()))
        ;

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
                sittingDate
            ))
                .thenReturn(Optional.of(Fee.builder()
                                            .standardFee(STANDARD_FEE)
                                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                            .build()));

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
                .thenReturn(Optional.of(Fee.builder()
                                            .standardFee(STANDARD_FEE)
                                            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
                                            .build()));

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

    @Test
    void shouldReturnPublishRecordWhenPopulatedWithErrorsAndFileInfos() {
        when(serviceService.findService(HMCTS_SERVICE_CODE))
            .thenReturn(Optional.of(
                Service.builder().serviceName(SERVICE_NAME).build()));


        when(sittingRecordRepository.findByStatusIdAndSittingDateLessThanEqual(
            SUBMITTED,
            LocalDate.now()
        ))
            .thenReturn(Streamable.of(
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord()
            ));

        doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addCourtVenueError(CourtVenueInError.builder()
                                                 .hmctsServiceId(HMCTS_SERVICE_CODE)
                                                 .epimmsId(EMPIMMS_ID)
                                                 .build());

            return null;
        }).doAnswer(invocation -> {
            PublishErrors publishErrors = invocation.getArgument(2, PublishErrors.class);
            publishErrors.setError(false);
            return null;
        }).when(publishErrorCheckerService).evaluate(
            eq(HMCTS_SERVICE_CODE),
            isA(SittingRecordPublishFields.class),
            isA(PublishErrors.class)
        );

        LocalDate localDate = LocalDate.of(2023, Month.OCTOBER, 30);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            InvocationOnMock::callRealMethod
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            PublishResponse publishResponse = publishSittingRecordService.publishRecords(
                HMCTS_SERVICE_CODE,
                LocalDate.now(),
                USER_ID,
                USER_NAME,
                false
            );

            assertThat(publishResponse.getFileInfos())
                .isNotNull();

            assertThat(publishResponse.getFileInfos().getFileInfos())
                .map(
                    FileInfo::getFileCreationDate,
                    FileInfo::getFileCreatedById,
                    FileInfo::getFileCreatedByName,
                    FileInfo::getRecordCount,
                    FileInfo::getFileName
                )
                .containsExactly(
                    tuple(localDate,
                          USER_ID,
                          USER_NAME,
                          1,
                          "SSCS_1_of_1_October_2023")
                );

            assertThat(publishResponse.getErrors().getCourtVenueInErrors())
                .map(CourtVenueInError::getHmctsServiceId,
                     CourtVenueInError::getEpimmsId)
                .containsExactly(
                    tuple(HMCTS_SERVICE_CODE,
                          EMPIMMS_ID)
                );
        }

    }

    @Test
    void shouldReturnPublishRecordWhenPopulatedWithOnlyFileInfosWithMultiFileInfoRecords() {
        when(applicationProperties.getMaximumNumberOfRecordsPerFile()).thenReturn(2);
        when(serviceService.findService(HMCTS_SERVICE_CODE))
            .thenReturn(Optional.of(
                Service.builder().serviceName(SERVICE_NAME).build()));


        when(sittingRecordRepository.findByStatusIdAndSittingDateLessThanEqual(
            SUBMITTED,
            LocalDate.now()
        ))
            .thenReturn(Streamable.of(
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord()
            ));


        LocalDate localDate = LocalDate.of(2023, Month.OCTOBER, 30);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            InvocationOnMock::callRealMethod
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            PublishResponse publishResponse = publishSittingRecordService.publishRecords(
                HMCTS_SERVICE_CODE,
                LocalDate.now(),
                USER_ID,
                USER_NAME,
                false
            );

            assertThat(publishResponse.getFileInfos().getFileInfos())
                .map(
                    FileInfo::getFileCreationDate,
                    FileInfo::getFileCreatedById,
                    FileInfo::getFileCreatedByName,
                    FileInfo::getRecordCount,
                    FileInfo::getFileName
                )
                .containsExactly(
                    tuple(localDate,
                          USER_ID,
                          USER_NAME,
                          2,
                          "SSCS_1_of_2_October_2023"),
                    tuple(localDate,
                          USER_ID,
                          USER_NAME,
                          1,
                          "SSCS_2_of_2_October_2023")
                );
        }
    }

    @Test
    void shouldReturnPublishRecordWhenPopulatedWithErrors() {
        when(serviceService.findService(HMCTS_SERVICE_CODE))
            .thenReturn(Optional.of(
                Service.builder().serviceName(SERVICE_NAME).build()));


        when(sittingRecordRepository.findByStatusIdAndSittingDateLessThanEqual(
            SUBMITTED,
            LocalDate.now()
        ))
            .thenReturn(Streamable.of(
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord(),
                getDefaultDbSittingRecord()
            ));

        doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addCourtVenueError(CourtVenueInError.builder().build());
            return null;
        }).doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addJohAttributesError(JohAttributesInError.builder().build());
            return null;
        }).doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addFeeError(FeeInError.builder().build());
            return null;
        }).doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addJohPayrollErrors(JohPayrollInError.builder().build());
            return null;
        }).doAnswer(invocation -> {
            PublishErrors publishErrors = getPublishErrors(invocation);
            publishErrors.addServiceError("NOT_PRESENT");
            return null;
        }).when(publishErrorCheckerService).evaluate(
            eq(HMCTS_SERVICE_CODE),
            isA(SittingRecordPublishFields.class),
            isA(PublishErrors.class)
        );

        LocalDate localDate = LocalDate.of(2023, Month.OCTOBER, 30);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            InvocationOnMock::callRealMethod
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            PublishResponse publishResponse = publishSittingRecordService.publishRecords(
                HMCTS_SERVICE_CODE,
                LocalDate.now(),
                USER_ID,
                USER_NAME,
                false
            );

            assertThat(publishResponse.getErrors().getRecordsInError())
                .isEqualTo(5);
            assertThat(publishResponse.getErrors().getCourtVenueInErrors())
                .isNotEmpty()
                .hasSize(1);
            assertThat(publishResponse.getErrors().getServiceInErrors())
                .isNotEmpty()
                .hasSize(1);
            assertThat(publishResponse.getErrors().getFeeInErrors())
                .isNotEmpty()
                .hasSize(1);
            assertThat(publishResponse.getErrors().getJohAttributesInErrors())
                .isNotEmpty()
                .hasSize(1);
            assertThat(publishResponse.getErrors().getJohPayrollInErrors())
                .isNotEmpty()
                .hasSize(1);
        }

    }

    @NotNull
    private static PublishErrors getPublishErrors(InvocationOnMock invocation) {
        PublishErrors publishErrors = invocation.getArgument(2, PublishErrors.class);
        publishErrors.setError(true);
        return publishErrors;
    }
}