package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class JudicialOfficeHolderRepositoryTest {
    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnCrownFlagWhenJohAttributesIsEffective() {
        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findByPersonalCode("7918178");
        assertThat(judicialOfficeHolder)
            .isPresent()
            .map(JudicialOfficeHolder::getIsActiveJohAttributesCrownFlag)
            .contains(true);
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnEmptyCrownFlagWhenJohAttributesIsMissing() {
        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findByPersonalCode("8918178");
        assertThat(judicialOfficeHolder)
            .isPresent()
            .map(JudicialOfficeHolder::getJohAttributes)
            .map(johAttributes -> johAttributes.isEmpty());

        assertThat(judicialOfficeHolder)
            .isPresent()
            .map(JudicialOfficeHolder::getIsActiveJohAttributesCrownFlag)
            .isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnEmptyCrownFlagWhenJohAttributesIsNotEffective() {
        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributes("9918178");

        Optional<LocalDate> effectiveStartDate = judicialOfficeHolder.stream()
            .map(JudicialOfficeHolder::getJohAttributes)
            .flatMap(Collection::stream)
            .filter(johAttributes1 -> johAttributes1.getId().equals(3L))
            .map(JohAttributes::getEffectiveStartDate)
            .findAny();

        assertThat(effectiveStartDate)
            .isPresent()
                .matches(startDate -> LocalDate.now().isBefore(startDate.get()));

        assertThat(judicialOfficeHolder)
            .isPresent()
            .map(JudicialOfficeHolder::getIsActiveJohAttributesCrownFlag)
            .isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnCrownFlagWhenJohAttributesIsBothEffectiveAndNonEffective() {
        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributes("9928178");

        Optional<JohAttributes> isJohAttributes = judicialOfficeHolder.stream()
            .map(JudicialOfficeHolder::getJohAttributes)
            .flatMap(Collection::stream)
            .filter(johAttributes -> LocalDate.now().isEqual(johAttributes.getEffectiveStartDate()))
            .findAny();

        assertThat(isJohAttributes)
            .isPresent();

        assertThat(judicialOfficeHolder)
            .isPresent()
            .map(JudicialOfficeHolder::getIsActiveJohAttributesCrownFlag)
            .contains(true);
    }
}

