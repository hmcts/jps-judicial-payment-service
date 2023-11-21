package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.FeeRepository;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitSittingRecordServiceTest {

    @Mock
    SittingRecordRepository sittingRecordRepository;

    @Mock
    SittingDaysService sittingDaysService;

    @Mock
    FeeRepository feeRepository;

    @Mock
    JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @Mock
    ApplicationProperties applicationProperties;

    @InjectMocks
    FeeService feeService;

    @InjectMocks
    JudicialOfficeHolderService judicialOfficeHolderService;

    SubmitSittingRecordService submitSittingRecordService;

    static final BigDecimal LONDON_THRESHOLD_FEE = new BigDecimal("2.5");
    static final BigDecimal STANDARD_FEE = new BigDecimal("1.5");
    static final BigDecimal HIGHER_THRESHOLD_FEE = new BigDecimal("2.1");

    @BeforeEach
    public void setUp() {
        submitSittingRecordService = new SubmitSittingRecordService(
            sittingRecordRepository,
            sittingDaysService,
            feeService,
            judicialOfficeHolderService,
            applicationProperties
        );
    }

    @ParameterizedTest
    @MockitoSettings(strictness = Strictness.LENIENT)
    @CsvSource(quoteCharacter = '"', textBlock = """
      # isMedicalMember, isLondonFlag, londonFee, isHigherMedicalRateSession, medicalThreshold, expectedFee
        true,    false, 2.5, false,  0, 2.1
        true,    false, 2.5, true,   0, 2.1
        true,    false, 2.5, false, 20, 1.5
        false,    true, 2.5, false,  0, 2.5
        false,    true,    , false,  0, 1.5
        false,   false, 2.5, false,  0, 2.5
        """)
    void calculateJohFeeForParams(boolean isMedicalMember, boolean isLondonFlag, BigDecimal londonFee,
                                  boolean isHigherMedicalSessionRate, int medicalThreshold, BigDecimal expectedFee) {

        Optional<Fee> ofResult = generateFee(londonFee, STANDARD_FEE, HIGHER_THRESHOLD_FEE);
        when(applicationProperties.isMedicalMember(anyString())).thenReturn(isMedicalMember);
        when(applicationProperties.getMedicalThreshold()).thenReturn(medicalThreshold);
        when(judicialOfficeHolderService.getLondonFlag(any(), any())).thenReturn(Optional.of(isLondonFlag));

        when(feeRepository.findByHmctsServiceIdAndJudgeRoleIdAndEffectiveFromIsLessThanEqual(
            any(),
            any(),
            any()
        )).thenReturn(ofResult);

        JohAttributes johAttributes2 = generateJohAttributes();
        JohAttributes johAttributes3 = generateJohAttributes();
        JohPayroll johPayroll2 = generateJohPayroll();
        JudicialOfficeHolder judicialOfficeHolder2 = generateJudicialOfficeHolder(johAttributes3, johPayroll2);

        JohPayroll johPayroll3 = generateJohPayroll(judicialOfficeHolder2);
        JudicialOfficeHolder judicialOfficeHolder3 = generateJudicialOfficeHolder(johAttributes2, johPayroll3);

        Optional<JudicialOfficeHolder> ofResult2 = Optional.of(judicialOfficeHolder3);
        when(judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributesFilteredByEffectiveStartDate(
            any(), any())).thenReturn(ofResult2);

        BigDecimal actualCalculateJohFeeResult = submitSittingRecordService.calculateJohFee(
            "hsc01",
            "PC001",
            "Judge",
            LocalDate.of(2023, 11, 1),
            isHigherMedicalSessionRate
        );

        verify(feeRepository).findByHmctsServiceIdAndJudgeRoleIdAndEffectiveFromIsLessThanEqual(
            any(),
            any(),
            any()
        );
        assertEquals(expectedFee, actualCalculateJohFeeResult);
    }

    private Optional<Fee> generateFee(BigDecimal londonWeightedFee, BigDecimal standardFee,
                                      BigDecimal higherThresholdFee) {
        Fee fee = new Fee();
        fee.setEffectiveFrom(LocalDate.of(2023, 11, 1));
        fee.setFeeCreatedDate(LocalDate.of(2023, 10, 1));
        fee.setHigherThresholdFee(higherThresholdFee);
        fee.setHmctsServiceId("hsid01");
        fee.setId(1L);
        fee.setJudgeRoleId("Judge");
        fee.setLondonWeightedFee(londonWeightedFee);
        fee.setStandardFee(standardFee);
        return Optional.of(fee);
    }

    private JohAttributes generateJohAttributes() {
        return generateJohAttributes(new JudicialOfficeHolder());
    }

    private JohAttributes generateJohAttributes(JudicialOfficeHolder judicialOfficeHolder) {
        JohAttributes johAttributes = new JohAttributes();
        johAttributes.setCrownServantFlag(true);
        johAttributes.setEffectiveStartDate(LocalDate.of(2023, 11, 1));
        johAttributes.setId(1L);
        johAttributes.setJudicialOfficeHolder(judicialOfficeHolder);
        johAttributes.setLondonFlag(true);
        return johAttributes;
    }

    private JohPayroll generateJohPayroll() {
        return generateJohPayroll(new JudicialOfficeHolder());
    }

    private JohPayroll generateJohPayroll(JudicialOfficeHolder judicialOfficeHolder) {
        JohPayroll johPayroll = new JohPayroll();
        johPayroll.setEffectiveStartDate(LocalDate.of(2023, 11, 1));
        johPayroll.setId(1L);
        johPayroll.setJudgeRoleTypeId("Judge");
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        johPayroll.setPayrollId("PRID01");
        return johPayroll;
    }

    private JudicialOfficeHolder generateJudicialOfficeHolder(JohAttributes johAttributes,
                                                              JohPayroll johPayroll) {
        JudicialOfficeHolder judicialOfficeHolder = new JudicialOfficeHolder();
        judicialOfficeHolder.addJohAttributes(johAttributes);
        judicialOfficeHolder.addJohPayroll(johPayroll);
        judicialOfficeHolder.setId(1L);
        judicialOfficeHolder.setPersonalCode("Personal Code");
        return judicialOfficeHolder;
    }
}
