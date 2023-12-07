package uk.gov.hmcts.reform.jps.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataHeaderRepository;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SubmitSittingRecordService extends PublishSittingRecordService {

    public SubmitSittingRecordService(SittingRecordRepository sittingRecordRepository,
                                      StatusHistoryService statusHistoryService,
                                      SittingRecordService sittingRecordService,
                                      SittingDaysService sittingDaysService, FeeService feeService,
                                      JudicialOfficeHolderService judicialOfficeHolderService,
                                      ApplicationProperties properties,
                                      PublishErrorCheckerService publishErrorCheckerService,
                                      SecurityUtils securityUtils,
                                      ServiceService serviceService,
                                      CourtVenueService courtVenueService,
                                      ExportedFileDataHeaderRepository exportedFileDataHeaderRepository,
                                      ExportedFileDataHeaderService exportedFileDataHeaderService,
                                      ExportedFileDataService exportedFileDataService,
                                      ExportedFilesService exportedFilesService) {

        super(sittingRecordRepository, statusHistoryService, sittingRecordService, sittingDaysService, feeService,
              judicialOfficeHolderService, properties, publishErrorCheckerService, securityUtils, serviceService,
              courtVenueService, exportedFileDataHeaderRepository, exportedFileDataHeaderService,
              exportedFileDataService, exportedFilesService);
    }

    @Override
    protected BigDecimal getMedicalMemberFee(
        String personalCode,
        LocalDate sittingDate,
        boolean higherMedicalRateSession,
        Fee fee) {
        BigDecimal derivedFee;
        if (higherMedicalRateSession) {
            derivedFee = fee.getHigherThresholdFee();
        } else {
            derivedFee = fee.getStandardFee();
        }
        return derivedFee;
    }

}
