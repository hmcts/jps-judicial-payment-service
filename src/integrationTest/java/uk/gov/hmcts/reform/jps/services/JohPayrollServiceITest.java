package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.repository.JohPayrollRepository;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JohPayrollServiceITest extends BaseTest {

    @Autowired
    private JohPayrollRepository johPayrollRepository;

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    private JohPayrollService johPayrollService;

    private JudicialOfficeHolderService judicialOfficeHolderService;

    @BeforeEach
    public void setUp() {
        johPayrollService = new JohPayrollService(johPayrollRepository);
        judicialOfficeHolderService = new JudicialOfficeHolderService(judicialOfficeHolderRepository);
    }

    /**
     * Method under test: {@link JohPayrollService#findById(Long)}.
     */
    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void testFindJohPayroll() {

        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder("PersonalCode1");
        JohPayroll johPayroll = createJohPayroll(LocalDate.of(1970, 1, 1),
                                                 "42",
                                                 "42");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        judicialOfficeHolderService.save(judicialOfficeHolder);
        johPayrollService.save(johPayroll);

        JudicialOfficeHolder judicialOfficeHolder2 = createJudicialOfficeHolder("PersonalCode2");
        JohPayroll johPayroll2 = createJohPayroll(LocalDate.of(1970, 1, 1),
                                                 "42",
                                                 "42");
        judicialOfficeHolder2.addJohPayroll(johPayroll2);
        johPayroll2.setJudicialOfficeHolder(judicialOfficeHolder2);
        judicialOfficeHolderService.save(judicialOfficeHolder2);
        johPayroll2 = johPayrollService.save(johPayroll2);
        Optional<JohPayroll> foundJohPayroll2 =  johPayrollService.findById(johPayroll2.getId());
        assertEquals(johPayroll2.getPayrollId(), foundJohPayroll2.get().getPayrollId());
        assertEquals(johPayroll2.getEffectiveStartDate(), foundJohPayroll2.get().getEffectiveStartDate());
        assertEquals(johPayroll2.getJudgeRoleTypeId(), foundJohPayroll2.get().getJudgeRoleTypeId());
    }

    /**
     * Method under test: {@link JohPayrollService#save(JohPayroll)}.
     */
    @Test
    @Sql(scripts = {"classpath:sql/reset_database.sql"})
    void testSaveJohPayroll() {

        JohPayroll johPayroll = createJohPayroll(LocalDate.of(1970, 1, 1),
                                                 "42",
                                                 "42");
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder("PersonalCode1");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        judicialOfficeHolderService.save(judicialOfficeHolder);
        johPayrollService.save(johPayroll);

        JohPayroll johPayroll2 = createJohPayroll(LocalDate.of(1970, 1, 1),
                                                  "42",
                                                  "42");
        JudicialOfficeHolder judicialOfficeHolder2 = createJudicialOfficeHolder("PersonalCode2");
        judicialOfficeHolder2.addJohPayroll(johPayroll2);
        johPayroll2.setJudicialOfficeHolder(judicialOfficeHolder2);
        judicialOfficeHolderService.save(judicialOfficeHolder2);
        johPayrollService.save(johPayroll2);

        JohPayroll johPayroll3 = createJohPayroll(LocalDate.of(1970, 1, 1),
                                                  "42",
                                                  "42");
        JudicialOfficeHolder judicialOfficeHolder3 = createJudicialOfficeHolder("PersonalCode3");
        judicialOfficeHolder3.addJohPayroll(johPayroll3);
        johPayroll3.setJudicialOfficeHolder(judicialOfficeHolder3);
        judicialOfficeHolderService.save(judicialOfficeHolder3);
        johPayrollService.save(johPayroll3);

        JohPayroll foundJohPayroll = johPayrollService.findById(johPayroll3.getId()).get();
        assertEqual(johPayroll3, foundJohPayroll);
    }

    private JudicialOfficeHolder createJudicialOfficeHolder(String personalCode) {
        return JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();
    }

    private JohPayroll createJohPayroll(LocalDate effectiveDate, String judgeRoleTypeId, String payrollId) {
        return JohPayroll.builder()
            .effectiveStartDate(effectiveDate)
            .judgeRoleTypeId(judgeRoleTypeId)
            .payrollId(payrollId)
            .build();
    }

    private void assertEqual(JohPayroll johPayroll, JohPayroll compareJohPayroll) {
        assertEquals(johPayroll.getId(), compareJohPayroll.getId());
        assertEquals(johPayroll.getPayrollId(), compareJohPayroll.getPayrollId());
        assertEquals(johPayroll.getEffectiveStartDate(), compareJohPayroll.getEffectiveStartDate());
        assertTrue(johPayroll.getJudicialOfficeHolder().equals(compareJohPayroll.getJudicialOfficeHolder()));
    }

}

