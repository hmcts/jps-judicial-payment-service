package uk.gov.hmcts.reform.jps.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

@ContextConfiguration(classes = {JudicialOfficeHolderService.class})
@ExtendWith(SpringExtension.class)
class JudicialOfficeHolderServiceTest {

    @MockBean
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @Autowired
    private JudicialOfficeHolderService judicialOfficeHolderService;

    /**
     * Method under test: {@link JudicialOfficeHolderService#findJudicialOfficeHolder(Long)}
     */
    @Test
    void testFindJudicialOfficeHolderById() {
        final String PERSONAL_CODE = "PersonalCode345";
        JudicialOfficeHolder judicialOfficeHolder = new JudicialOfficeHolder();
        judicialOfficeHolder.setId(1L);
        judicialOfficeHolder.setPersonalCode(PERSONAL_CODE);
        Optional<JudicialOfficeHolder> ofResult = Optional.of(judicialOfficeHolder);
        when(judicialOfficeHolderRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        assertSame(judicialOfficeHolder, judicialOfficeHolderService.findJudicialOfficeHolder(1L));
        verify(judicialOfficeHolderRepository).findById(Mockito.<Long>any());
    }

    /**
     * Method under test: {@link JudicialOfficeHolderService#findJudicialOfficeHolder(String)}
     */
    @Test
    void testFindJudicialOfficeHolderByPersonalCode() {
        final String PERSONAL_CODE = "PersonalCode777";
        JudicialOfficeHolder judicialOfficeHolder = new JudicialOfficeHolder();
        judicialOfficeHolder.setId(1L);
        judicialOfficeHolder.setPersonalCode(PERSONAL_CODE);
        when(judicialOfficeHolderRepository.findByPersonalCode(Mockito.<String>any())).thenReturn(judicialOfficeHolder);
        assertSame(judicialOfficeHolder, judicialOfficeHolderService.findJudicialOfficeHolder(PERSONAL_CODE));
        verify(judicialOfficeHolderRepository).findByPersonalCode(Mockito.<String>any());
    }
}

