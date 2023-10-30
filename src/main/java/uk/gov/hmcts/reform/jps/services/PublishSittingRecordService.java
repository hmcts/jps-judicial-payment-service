package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.model.FinancialYearRecords;
import uk.gov.hmcts.reform.jps.model.PublishSittingRecordCount;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublishSittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;
    private final SittingDaysService sittingDaysService;
    private final FeeService feeService;
    private final JudicialOfficeHolderService judicialOfficeHolderService;
    private final ApplicationProperties properties;


    public PublishSittingRecordCount retrievePublishedRecords(String personalCode) {
        LocalDate currentDate = LocalDate.now();
        String currentFinancialYear = getFinancialYear(currentDate);

        FinancialYearRecords currentFinancialYearRecords = FinancialYearRecords.builder()
            .publishedCount(sittingDaysService.getSittingCount(personalCode, currentFinancialYear))
            .submittedCount(
                sittingRecordRepository.findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
                    personalCode,
                    SUBMITTED,
                    startOfFinancialYear(currentDate),
                    endOfFinancialYear(currentDate))
            )
            .financialYear(getFinancialYear(currentDate))
            .build();

        LocalDate previousYear = currentDate.minusYears(1L);
        String previousFinancialYear = getFinancialYear(previousYear);
        FinancialYearRecords previousFinancialYearRecords = FinancialYearRecords.builder()
            .publishedCount(sittingDaysService.getSittingCount(personalCode, previousFinancialYear))
            .submittedCount(
                sittingRecordRepository.findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
                    personalCode,
                    SUBMITTED,
                    startOfFinancialYear(previousYear),
                    endOfFinancialYear(previousYear))
            )
            .financialYear(getFinancialYear(previousYear))
            .build();

        return PublishSittingRecordCount.builder()
            .currentFinancialYear(currentFinancialYearRecords)
            .previousFinancialYear(previousFinancialYearRecords)
            .build();
    }

    private String getFinancialYear(LocalDate date) {
        int year = date.getYear();
        int nextYear = (year + 1) % 100;
        return String.join("-",
                           String.valueOf(year), String.valueOf(nextYear));

    }

    private LocalDate startOfFinancialYear(LocalDate date) {
        return LocalDate.of(date.getYear(),
                            Month.APRIL,
                            6);
    }

    private LocalDate endOfFinancialYear(LocalDate date) {
        return LocalDate.of(date.getYear() + 1,
                            Month.APRIL,
                            5);
    }

    public BigDecimal calculateJohFee(
        String hmctsServiceCode,
        String personalCode,
        String judgeRoleTypeId,
        LocalDate sittingDate,
        boolean higherMedicalRateSession) {
        Fee fee = feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            hmctsServiceCode,
            judgeRoleTypeId,
            sittingDate
        );

        if (properties.isMedicalMember(judgeRoleTypeId)) {
            return getMedicalMemberFee(personalCode, sittingDate, higherMedicalRateSession, fee);
        } else {
            return getNonMedicalMemberFee(personalCode, sittingDate, fee);
        }
    }

    private BigDecimal getNonMedicalMemberFee(String personalCode, LocalDate sittingDate, Fee fee) {
        return judicialOfficeHolderService.getLondonFlag(personalCode, sittingDate)
            .filter(flag -> flag == Boolean.TRUE
                    && Objects.nonNull(fee.getLondonWeightedFee()))
            .map(flag -> fee.getLondonWeightedFee())
            .orElse(fee.getStandardFee());
    }

    private BigDecimal getMedicalMemberFee(
        String personalCode,
        LocalDate sittingDate,
        boolean higherMedicalRateSession,
        Fee fee) {
        BigDecimal derivedFee;
        String sittingDateFinancialYear = getFinancialYear(sittingDate);
        PublishSittingRecordCount publishSittingRecordCount = retrievePublishedRecords(personalCode);
        long publishedSittingCount = getPublishedSittingCount(publishSittingRecordCount, sittingDateFinancialYear);
        if (publishedSittingCount > properties.getMedicalThreshold() || higherMedicalRateSession) {
            derivedFee = fee.getHigherThresholdFee();
        } else {
            derivedFee = fee.getStandardFee();
        }
        return derivedFee;
    }

    private long getPublishedSittingCount(
        PublishSittingRecordCount publishSittingRecordCount,
        String sittingDateFinancialYear) {
        long publishedSittingCount;

        if (publishSittingRecordCount.getCurrentFinancialYear().getFinancialYear().equals(sittingDateFinancialYear)) {
            publishedSittingCount = publishSittingRecordCount.getCurrentFinancialYear().getPublishedCount() + 1;
        } else if (publishSittingRecordCount.getPreviousFinancialYear().getFinancialYear().equals(
            sittingDateFinancialYear)) {
            publishedSittingCount = publishSittingRecordCount.getPreviousFinancialYear().getPublishedCount() + 1;
        } else {
            throw new IllegalArgumentException("Financial year is invalid : " + sittingDateFinancialYear);
        }
        return publishedSittingCount;
    }
}
