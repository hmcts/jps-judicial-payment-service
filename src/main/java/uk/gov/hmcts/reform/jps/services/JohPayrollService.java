package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.repository.JohPayrollRepository;

import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JohPayrollService {

    private final JohPayrollRepository johPayrollRepository;

    public Optional<JohPayroll> findById(Long johId) {
        return  johPayrollRepository.findById(johId);
    }

    public JohPayroll save(JohPayroll johPayroll) {
        return johPayrollRepository.save(johPayroll);
    }

}
