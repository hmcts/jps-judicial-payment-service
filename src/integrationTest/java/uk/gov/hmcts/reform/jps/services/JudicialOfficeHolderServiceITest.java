package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JudicialOfficeHolderServiceITest extends BaseTest {

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @Test
    void shouldObtainSaveJudicialOfficeHolder() {
        final String Personal_Code = "PC111";
        JudicialOfficeHolder persistedJudicialOfficeHolder = createAndSaveJudicialOfficeHolder(Personal_Code);

        JudicialOfficeHolder obtainedJudicialOfficeHolder  =
            judicialOfficeHolderRepository.findById(persistedJudicialOfficeHolder.getId()).get();
        assertEquals(persistedJudicialOfficeHolder.getId(), obtainedJudicialOfficeHolder.getId());
        assertEquals(persistedJudicialOfficeHolder.getPersonalCode(), obtainedJudicialOfficeHolder.getPersonalCode());
        assertEquals(persistedJudicialOfficeHolder.getJohPayrolls().size(),
                     obtainedJudicialOfficeHolder.getJohPayrolls().size());
    }

    private JudicialOfficeHolder createAndSaveJudicialOfficeHolder(String personalCode) {
        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();
        return judicialOfficeHolderRepository.save(judicialOfficeHolder);
    }
}
