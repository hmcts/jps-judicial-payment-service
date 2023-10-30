package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.FileInfo;
import uk.gov.hmcts.reform.jps.model.FileInfos;
import uk.gov.hmcts.reform.jps.model.FinancialYearRecords;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.model.PublishSittingRecordCount;
import uk.gov.hmcts.reform.jps.model.out.PublishResponse;
import uk.gov.hmcts.reform.jps.model.out.PublishResponse.PublishResponseBuilder;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublishSittingRecordService {

    private final SittingRecordRepository sittingRecordRepository;
    private final SittingDaysService sittingDaysService;
    private final FeeService feeService;
    private final JudicialOfficeHolderService judicialOfficeHolderService;
    private final ApplicationProperties properties;
    private final PublishErrorCheckerService publishErrorCheckerService;
    private final ServiceService serviceService;


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
                    endOfFinancialYear(currentDate)
                )
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
                    endOfFinancialYear(previousYear)
                )
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
                           String.valueOf(year), String.valueOf(nextYear)
        );

    }

    private LocalDate startOfFinancialYear(LocalDate date) {
        return LocalDate.of(
            date.getYear(),
            Month.APRIL,
            6
        );
    }

    private LocalDate endOfFinancialYear(LocalDate date) {
        return LocalDate.of(
            date.getYear() + 1,
            Month.APRIL,
            5
        );
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
        ).orElseThrow(() -> new IllegalArgumentException(
            String.join(
                " ",
                "Fee not set/active for hmctsServiceCode",
                hmctsServiceCode,
                "and judgeRoleTypeId",
                judgeRoleTypeId
            )));


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

    public PublishResponse publishRecords(String hmctsServiceCode,
                                          LocalDate dateRangeTo,
                                          String publishedByIdamId,
                                          String publishedByName,
                                          boolean publish) {
        PublishErrors publishErrors
            = PublishErrors.builder().build();

        String serviceName = serviceService.findService(hmctsServiceCode)
            .map(uk.gov.hmcts.reform.jps.domain.Service::getServiceName)
            .orElse("N/A");

        FileInfos fileInfos = FileInfos.builder().build();

        try (Stream<SittingRecordPublishFields> stream =
                 sittingRecordRepository.findByStatusIdAndSittingDateLessThanEqual(
                     SUBMITTED,
                     dateRangeTo
                 ).stream()) {
            stream.forEach(
                sittingRecordPublishFields -> {
                    publishErrors.setError(false);
                    publishErrorCheckerService.evaluate(
                        hmctsServiceCode,
                        sittingRecordPublishFields,
                        publishErrors
                    );
                    if (!publishErrors.isError()) {
                        FileInfo fileInfo = getFileInfo(fileInfos,
                                                        serviceName,
                                                        publishedByIdamId,
                                                        publishedByName);
                    }
                }
            );
        }
        PublishResponseBuilder publishResponseBuilder = PublishResponse.builder();
        setFileNames(fileInfos, publishResponseBuilder);
        int errorCount = publishErrors.getErrorCount();
        if (errorCount > 0) {
            publishErrors.setRecordsInError(errorCount);
            publishResponseBuilder.errors(publishErrors);
        }
        return publishResponseBuilder.build();
    }

    private void setFileNames(FileInfos fileInfos, PublishResponseBuilder publishResponseBuilder) {
        if (fileInfos.getFileCount() > 0) {
            fileInfos.setFileNames();
            publishResponseBuilder.fileInfos(fileInfos);
        }
    }

    private FileInfo getFileInfo(FileInfos fileInfos,
                                 String serviceName,
                                 String publishedByIdamId,
                                 String publishedByName) {
        if (fileInfos.getFileCount() == 0) {
            return fileInfos.createFileInfo(fileInfos,
                                     serviceName,
                                     publishedByIdamId,
                                     publishedByName);
        } else {
            FileInfo fileInfo = fileInfos.getLatestFileInfo();
            int incrementRecordCount = fileInfo.getRecordCount() + 1;
            if (incrementRecordCount <= properties.getMaximumNumberOfRecordsPerFile()) {
                fileInfo.setRecordCount(incrementRecordCount);
            } else {
                fileInfos.createFileInfo(fileInfos,
                                         serviceName,
                                         publishedByIdamId,
                                         publishedByName);
            }
            return fileInfo;
        }
    }
}
