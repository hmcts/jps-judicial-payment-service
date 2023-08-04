package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialOfficeHolderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JudicialOfficeHolderService.class);

    private final JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    public JudicialOfficeHolder findJudicialOfficeHolder(Long johId) {
        return judicialOfficeHolderRepository.findById(johId).get();
    }

    public JudicialOfficeHolder findJudicialOfficeHolder(String personalCode) {
        return judicialOfficeHolderRepository.findByPersonalCode(personalCode);
    }

}
