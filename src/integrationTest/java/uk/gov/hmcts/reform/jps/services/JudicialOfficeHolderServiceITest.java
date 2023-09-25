package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class JudicialOfficeHolderServiceITest extends BaseTest {

    @Autowired
    private JudicialOfficeHolderService judicialOfficeHolderService;

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldObtainSaveJudicialOfficeHolder() {
        final String Personal_Code = "PC111";
        JudicialOfficeHolder persistedJudicialOfficeHolder = createAndSaveJudicialOfficeHolder(Personal_Code);

        JudicialOfficeHolder obtainedJudicialOfficeHolder  =
            judicialOfficeHolderService.findById(persistedJudicialOfficeHolder.getId()).get();
        assertEquals(persistedJudicialOfficeHolder.getId(), obtainedJudicialOfficeHolder.getId());
        assertEquals(persistedJudicialOfficeHolder.getPersonalCode(), obtainedJudicialOfficeHolder.getPersonalCode());
        assertEquals(persistedJudicialOfficeHolder.getJohPayrolls().size(),
                     obtainedJudicialOfficeHolder.getJohPayrolls().size());
    }

    @Test
    @Sql(scripts = {RESET_DATABASE})
    void shouldObtainSavedJudicialOfficeHolderByPersonalCode() {
        final String Personal_Code = "PC111";
        JudicialOfficeHolder persistedJudicialOfficeHolder = createAndSaveJudicialOfficeHolder(Personal_Code);

        Optional<JudicialOfficeHolder> obtainedJudicialOfficeHolder  =
            judicialOfficeHolderService.findByPersonalCode(Personal_Code);

        assertThat(obtainedJudicialOfficeHolder)
            .map(JudicialOfficeHolder::getId)
            .hasValue(persistedJudicialOfficeHolder.getId());

        assertThat(obtainedJudicialOfficeHolder)
            .map(JudicialOfficeHolder::getPersonalCode)
            .hasValue(persistedJudicialOfficeHolder.getPersonalCode());

        assertThat(obtainedJudicialOfficeHolder)
            .map(judicialOfficeHolder ->
                     judicialOfficeHolder.getJohPayrolls().size())
            .hasValue(persistedJudicialOfficeHolder.getJohPayrolls().size());

    }

    @ParameterizedTest
    @CsvSource(textBlock = """
      # SITTING_DATE,       CROWN_FLAG
      '2023-09-04',         false
      '2023-09-03',         true
        """)
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnLatestRecordCrownFlagWhenMoreThanOneJohAttributePresent(LocalDate sittingDate, boolean crownFlag) {
        Optional<Boolean> crownServiceFlag = judicialOfficeHolderService.getCrownServiceFlag(
            "9938178",
            sittingDate
        );

        assertThat(crownServiceFlag)
            .hasValue(crownFlag);
    }


    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnEmptyCrownFlagWhenNoJohAttributePresent() {
        Optional<Boolean> crownServiceFlag = judicialOfficeHolderService.getCrownServiceFlag(
            "999999",
            LocalDate.now()
        );

        assertThat(crownServiceFlag)
            .isEmpty();
    }

    private JudicialOfficeHolder createAndSaveJudicialOfficeHolder(String personalCode) {
        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();
        return judicialOfficeHolderService.save(judicialOfficeHolder);
    }
}
