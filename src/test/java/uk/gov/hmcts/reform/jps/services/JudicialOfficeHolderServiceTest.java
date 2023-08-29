package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {JudicialOfficeHolderService.class})
@ExtendWith(SpringExtension.class)
class JudicialOfficeHolderServiceTest {

    @MockBean
    private JudicialOfficeHolderService judicialOfficeHolderService;

    /**
     * Method under test: {@link JudicialOfficeHolderService#findById(Long)}.
     */
    @Test
    void testFindJudicialOfficeHolderById() {
        final String Personal_Code = "PersonalCode345";
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, Personal_Code);
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.now(), "jr1111", "pr11222");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        Optional<JudicialOfficeHolder> ofResult = Optional.of(judicialOfficeHolder);
        when(judicialOfficeHolderService.findById(1L)).thenReturn(ofResult);
        assertEquals(judicialOfficeHolder.getId(), judicialOfficeHolderService.findById(1L).get().getId());
        verify(judicialOfficeHolderService).findById(Mockito.<Long>any());
    }

    /**
     * Method under test: {@link JudicialOfficeHolderService#findByPersonalCode(String)}.
     */
    @Test
    void testFindJudicialOfficeHolderByPersonalCode() {
        final String Personal_Code = "PersonalCode777";
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, Personal_Code);
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.now(), "jr1111", "pr11222");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        when(judicialOfficeHolderService.findByPersonalCode(Personal_Code)).thenReturn(judicialOfficeHolder);
        assertSame(judicialOfficeHolder, judicialOfficeHolderService.findByPersonalCode(Personal_Code));
        verify(judicialOfficeHolderService).findByPersonalCode(Mockito.<String>any());
    }


    private JudicialOfficeHolder createJudicialOfficeHolder(Long id, String personalCode) {
        return JudicialOfficeHolder.builder()
            .id(id)
            .personalCode(personalCode)
            .build();
    }

    private JohPayroll createJohPayroll(Long id, LocalDate effectiveDate, String judgeRoleTypeId, String payrollId) {
        return JohPayroll.builder()
            .id(id)
            .effectiveStartDate(effectiveDate)
            .judgeRoleTypeId(judgeRoleTypeId)
            .payrollId(payrollId)
            .build();
    }

}

