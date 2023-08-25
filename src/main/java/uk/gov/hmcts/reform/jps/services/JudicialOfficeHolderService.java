package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialOfficeHolderService {

    private final JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    public Optional<JudicialOfficeHolder> findById(Long johId) {
        return judicialOfficeHolderRepository.findById(johId);
    }

    public Optional<JudicialOfficeHolder> findByPersonalCode(String personalCode) {
        return judicialOfficeHolderRepository.findByPersonalCode(personalCode);
    }

    public JudicialOfficeHolder save(JudicialOfficeHolder judicialOfficeHolder) {
        return judicialOfficeHolderRepository.save(judicialOfficeHolder);
    }

    public Optional<Boolean> getCrownServiceFlag(String personalCode) {
        return judicialOfficeHolderRepository.findByPersonalCode(personalCode)
            .map(JudicialOfficeHolder::getIsActiveJohAttributesCrownFlag);
    }
}
