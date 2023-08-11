package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.repository.JohPayrollRepository;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JohPayrollService {

    private final JohPayrollRepository johPayrollRepository;

    public JohPayroll findById(Long johId) {
        return  johPayrollRepository.findById(johId).orElse(null);
    }

    public JohPayroll save(JohPayroll johPayroll) {
        return johPayrollRepository.save(johPayroll);
    }

}
