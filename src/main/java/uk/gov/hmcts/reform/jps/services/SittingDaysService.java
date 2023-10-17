package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.repository.SittingDaysRepository;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingDaysService {

    private final SittingDaysRepository sittingDaysRepository;

    public Long getSittingCount(String personalCode, String financialYear) {
        return sittingDaysRepository.findSittingCountByPersonalCodeAndFinancialYear(personalCode, financialYear)
            .orElse(0L);
    }
}
