package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.repository.SittingDaysRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingDaysService {

    private final SittingDaysRepository sittingDaysRepository;

    public Long getSittingCount(String personalCode, String financialYear) {
        return sittingDaysRepository.findSittingCountByPersonalCodeAndFinancialYear(personalCode, financialYear)
            .orElse(0L);
    }

    @Transactional
    public void updateSittingCount(Long sittingCount, String judgeRoleTypeId, String personalCode,
                                   String financialYear) {
        sittingDaysRepository.updateSittingCount(sittingCount, judgeRoleTypeId, personalCode, financialYear);
    }

}
