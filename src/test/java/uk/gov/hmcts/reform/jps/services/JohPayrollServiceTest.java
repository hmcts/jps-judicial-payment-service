package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JohPayrollRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {JohPayrollService.class})
@ExtendWith(SpringExtension.class)
class JohPayrollServiceTest {
    @MockBean
    private JohPayrollRepository johPayrollRepository;

    @Autowired
    private JohPayrollService johPayrollService;

    /**
     * Method under test: {@link JohPayrollService#findById(Long)}.
     */
    @Test
    void testFindById() {
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, "Personal Code");
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.of(2023, 1, 1),
                                                 "jr111", "pr11111");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);

        JohPayroll johPayroll2 = createJohPayroll(1L, LocalDate.of(2023, 1, 1),
                                                 "jr2222", "pr22222");
        JudicialOfficeHolder judicialOfficeHolder2 = createJudicialOfficeHolder(2L, "Personal Code 2");
        johPayroll2.setJudicialOfficeHolder(judicialOfficeHolder2);
        Optional<JohPayroll> ofResult = Optional.of(johPayroll2);
        when(johPayrollService.findById(Mockito.<Long>any())).thenReturn(ofResult);
        Optional<JohPayroll> actualFindByIdResult = johPayrollService.findById(1L);
        assertSame(ofResult, actualFindByIdResult);
        assertTrue(actualFindByIdResult.isPresent());
        verify(johPayrollRepository).findById(Mockito.<Long>any());
    }

    /**
     * Method under test: {@link JohPayrollService#save(JohPayroll)}.
     */
    @Test
    void testSave() {
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.of(2023, 1, 1),
                                                 "jr111", "pr11111");
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, "Personal Code");
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        judicialOfficeHolder.addJohPayroll(johPayroll);

        JohPayroll johPayroll2 = createJohPayroll(2L, LocalDate.of(2023, 1, 1),
                                                 "jr222", "pr22222");
        JudicialOfficeHolder judicialOfficeHolder2 = createJudicialOfficeHolder(2L, "Personal Code 2");
        judicialOfficeHolder2.addJohPayroll(johPayroll2);

        JohPayroll johPayroll3 = createJohPayroll(2L, LocalDate.of(2023, 1, 1),
                                                  "jr333", "pr33333");
        johPayroll3.setJudicialOfficeHolder(judicialOfficeHolder2);
        when(johPayrollService.save(Mockito.<JohPayroll>any())).thenReturn(johPayroll3);

        JudicialOfficeHolder judicialOfficeHolder3 = createJudicialOfficeHolder(3L, "Personal Code 3");
        judicialOfficeHolder3.addJohPayroll(johPayroll3);

        JohPayroll johPayroll4 = createJohPayroll(4L, LocalDate.of(2023, 1, 1),
                                                  "jr4444", "pr44444");
        JudicialOfficeHolder judicialOfficeHolder4 = createJudicialOfficeHolder(4L, "Personal Code 4");
        johPayroll4.setJudicialOfficeHolder(judicialOfficeHolder4);
        judicialOfficeHolder4.addJohPayroll(johPayroll4);

        JohPayroll johPayroll5 = createJohPayroll(5L, LocalDate.of(2023, 1, 1),
                                                  "jr5555", "pr55555");
        johPayroll5.setJudicialOfficeHolder(judicialOfficeHolder4);
        judicialOfficeHolder4.addJohPayroll(johPayroll5);

        assertSame(johPayroll3, johPayrollService.save(johPayroll5));
        verify(johPayrollRepository).save(Mockito.<JohPayroll>any());
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

