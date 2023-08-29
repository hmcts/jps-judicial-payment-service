package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
    @Autowired
    private SittingRecordRepository recordRepository;
    private static final String PERSONAL_CODE = "001";


    @BeforeEach
    public void setUp() {
        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(PERSONAL_CODE)
            .build();
        judicialOfficeHolderRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = SittingRecord.builder()
            .am(true)
            .contractTypeId(2L)
            .epimmsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(PERSONAL_CODE)
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .build();

        StatusHistory statusHistory = StatusHistory.builder()
            .changedByName("John Doe")
            .changedByUserId("jp-recorder")
            .changedDateTime(LocalDateTime.now())
            .statusId(StatusId.RECORDED)
            .build();
        sittingRecord.addStatusHistory(statusHistory);

        recordRepository.save(sittingRecord);

        List<JudicialOfficeHolder> list = judicialOfficeHolderRepository.findAll();
        assertThat(list).isNotEmpty();
    }

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
}

