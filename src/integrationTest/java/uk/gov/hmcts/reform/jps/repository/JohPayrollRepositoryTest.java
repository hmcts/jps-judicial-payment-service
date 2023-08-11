package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class JohPayrollRepositoryTest {

    @Autowired
    private JohPayrollRepository johPayrollRepository;
    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    private static final String PERSONAL_CODE = "PC1111";

    private JudicialOfficeHolder judicialOfficeHolder;

    @BeforeEach
    public void setUp() {
        johPayrollRepository.deleteAll();
        judicialOfficeHolderRepository.deleteAll();
        judicialOfficeHolder = createJudicialOfficeHolder(PERSONAL_CODE);
    }

    @Test
    void shouldSaveJohPayRoll() {
        JohPayroll johPayroll = createJohPayroll(judicialOfficeHolder, LocalDate.now().plusDays(2),
                                                 "jrt11100", "prid112222"
        );
        JohPayroll persistedJohPayroll = johPayrollRepository.save(johPayroll);

        JohPayroll foundJohPayroll = johPayrollRepository.findById(persistedJohPayroll.getId()).get();

        assertNotNull(foundJohPayroll);
        assertEquals(persistedJohPayroll, foundJohPayroll);
    }

    @Test
    void shouldSaveMultipleJohPayRoll() {
        JohPayroll johPayroll1 = createJohPayroll(judicialOfficeHolder, LocalDate.now().plusDays(2),
                                                 "jrt11100", "prid112222");
        JohPayroll persistedJohPayroll1 = johPayrollRepository.save(johPayroll1);

        JohPayroll johPayroll2 = createJohPayroll(judicialOfficeHolder, LocalDate.now().plusDays(2),
                                                  "jrt11101", "prid112223");
        JohPayroll persistedJohPayroll2 = johPayrollRepository.save(johPayroll2);

        JohPayroll johPayroll3 = createJohPayroll(judicialOfficeHolder, LocalDate.now().plusDays(2),
                                                  "jrt11102", "prid112224");
        JohPayroll persistedJohPayroll3 = johPayrollRepository.save(johPayroll3);


        JohPayroll foundJohPayroll1 = johPayrollRepository.findById(persistedJohPayroll1.getId()).get();
        JohPayroll foundJohPayroll2 = johPayrollRepository.findById(persistedJohPayroll2.getId()).get();
        JohPayroll foundJohPayroll3 = johPayrollRepository.findById(persistedJohPayroll3.getId()).get();

        assertNotNull(foundJohPayroll1);
        assertNotNull(foundJohPayroll2);
        assertNotNull(foundJohPayroll3);
        assertEquals(persistedJohPayroll1, foundJohPayroll1);
        assertEquals(persistedJohPayroll1.getJudicialOfficeHolder(), judicialOfficeHolder);
        assertEquals(persistedJohPayroll2.getJudicialOfficeHolder(), judicialOfficeHolder);
        assertEquals(persistedJohPayroll3.getJudicialOfficeHolder(), judicialOfficeHolder);
    }

    private JudicialOfficeHolder createJudicialOfficeHolder(String personalCode) {
        JudicialOfficeHolder judicialOfficeHolderNew = JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();
        return judicialOfficeHolderRepository.save(judicialOfficeHolderNew);
    }

    private JohPayroll createJohPayroll(JudicialOfficeHolder judicialOfficeHolder,
                                        LocalDate effectiveStartDate,
                                        String judgeRoleTypeId,
                                        String payrollId
    ) {
        JohPayroll johPayroll = JohPayroll.builder()
            .effectiveStartDate(effectiveStartDate)
            .judgeRoleTypeId(judgeRoleTypeId)
            .judicialOfficeHolder(judicialOfficeHolder)
            .payrollId(payrollId)
            .build();
        judicialOfficeHolder.addJohPayroll(johPayroll);
        return johPayroll;
    }

}
