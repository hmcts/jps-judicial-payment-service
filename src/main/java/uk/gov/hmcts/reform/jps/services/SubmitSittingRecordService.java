package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.domain.Fee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SubmitSittingRecordService {
    private final FeeService feeService;
    private final JudicialOfficeHolderService judicialOfficeHolderService;
    private final ApplicationProperties properties;

    public BigDecimal calculateJohFee(String hmctsServiceCode, String personalCode, String judgeRoleTypeId,
                                      LocalDate sittingDate) {
        Fee fee = feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            hmctsServiceCode,
            judgeRoleTypeId,
            sittingDate
        );

        if (properties.isMedicalMember(judgeRoleTypeId)) {
            return fee.getStandardFee();
        } else {
            return getNonMedicalMemberFee(personalCode, sittingDate, fee);
        }
    }

    private BigDecimal getNonMedicalMemberFee(String personalCode, LocalDate sittingDate, Fee fee) {
        return judicialOfficeHolderService.getLondonFlag(personalCode, sittingDate)
            .filter(flag -> flag.equals(Boolean.TRUE)
                    && Objects.nonNull(fee.getLondonWeightedFee()))
            .map(flag -> fee.getLondonWeightedFee())
            .orElse(fee.getStandardFee());
    }

}
