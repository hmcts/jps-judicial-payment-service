package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialOfficeHolderService {

    private final JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    public JudicialOfficeHolder findJudicialOfficeHolder(Long johId) {
        return judicialOfficeHolderRepository.findById(johId).orElse(null);
    }

    public JudicialOfficeHolder findJudicialOfficeHolder(String personalCode) {
        return judicialOfficeHolderRepository.findByPersonalCode(personalCode);
    }

    public JudicialOfficeHolder save(JudicialOfficeHolder judicialOfficeHolder) {
        return judicialOfficeHolderRepository.save(judicialOfficeHolder);
    }

}
