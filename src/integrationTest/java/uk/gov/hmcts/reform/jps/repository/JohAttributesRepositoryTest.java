package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class JohAttributesRepositoryTest {
    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @Autowired
    private JohAttributesRepository johAttributesRepository;

    @BeforeEach
    void setUp() {
        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
                .personalCode("123")
                .build();
        JohAttributes johAttributes = JohAttributes.builder()
                .effectiveStartDate(LocalDate.now())
                .crownServantFlag(true)
                .londonFlag(true)
                .build();

        judicialOfficeHolder.addJohAttributes(johAttributes);
        judicialOfficeHolderRepository.save(judicialOfficeHolder);
    }


    @Test
    void shouldSaveJohAttributes() {
        List<JohAttributes> johAttributesList = johAttributesRepository.findAll();
        JohAttributes johAttributes = johAttributesList.get(0);
        assertThat(johAttributesList).isNotNull();
        assertThat(johAttributesList).hasSize(1);
        assertThat(johAttributes.getId()).isNotNull();
        assertThat(johAttributes.getEffectiveStartDate()).isEqualTo(LocalDate.now());
        assertThat(johAttributes.isCrownServantFlag()).isTrue();
        assertThat(johAttributes.isLondonFlag()).isTrue();
    }


    @Test
    void shouldUpdateJohAttributesWhenRecordIsPresent() {
        List<JohAttributes> johAttributesList = johAttributesRepository.findAll();
        JohAttributes johAttributes = johAttributesList.get(0);

        johAttributes.setCrownServantFlag(false);
        johAttributes.setLondonFlag(false);
        LocalDate effectiveStartDate = LocalDate.now().plusDays(2);
        johAttributes.setEffectiveStartDate(effectiveStartDate);

        johAttributesRepository.save(johAttributes);

        Optional<JohAttributes> optionalJohAttributes = johAttributesRepository.findById(johAttributes.getId());


        assertThat(optionalJohAttributes).isPresent();
        assertThat(optionalJohAttributes.get().getId()).isEqualTo(johAttributes.getId());
        assertThat(optionalJohAttributes.get().getEffectiveStartDate()).isEqualTo(effectiveStartDate);
        assertThat(optionalJohAttributes.get().isCrownServantFlag()).isFalse();
        assertThat(optionalJohAttributes.get().isLondonFlag()).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<JohAttributes> optionalJohAttributes = johAttributesRepository.findById(100L);
        assertThat(optionalJohAttributes).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        List<JohAttributes> johAttributesList = johAttributesRepository.findAll();
        JohAttributes johAttributes = johAttributesList.get(0);
        johAttributesRepository.delete(johAttributes);
        Optional<JohAttributes> optionalJohAttributesDeleted = johAttributesRepository.findById(johAttributes.getId());
        assertThat(optionalJohAttributesDeleted).isEmpty();
    }
}
