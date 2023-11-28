package uk.gov.hmcts.reform.jps.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SubmitSittingRecordService extends PublishSittingRecordService {

    public SubmitSittingRecordService(SittingRecordRepository sittingRecordRepository,
                                      SittingDaysService sittingDaysService, FeeService feeService,
                                      JudicialOfficeHolderService judicialOfficeHolderService,
                                      ApplicationProperties properties) {
        super(sittingRecordRepository, sittingDaysService, feeService, judicialOfficeHolderService, properties);
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
