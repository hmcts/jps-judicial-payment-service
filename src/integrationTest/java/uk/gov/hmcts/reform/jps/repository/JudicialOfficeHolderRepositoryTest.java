package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("crownFlagForSittingDate")
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnCrownFlagWhenJohAttributesIsEffective(
        String personalCode,
        LocalDate sittingDate,
        boolean crownFlag) {

        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributesFilteredByEffectiveStartDate(
            personalCode,
                sittingDate);


        assertThat(judicialOfficeHolder.stream())
            .flatMap(JudicialOfficeHolder::getJohAttributes)
            .extracting(JohAttributes::isCrownServantFlag)
            .containsOnly(crownFlag);
    }

    private static Stream<Arguments> crownFlagForSittingDate() {
        return Stream.of(
            Arguments.of("7918178", LocalDate.of(2023, Month.APRIL, 27), true),
            Arguments.of("9928178", LocalDate.now(), false)
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
      # PERSONAL_CODE
      '8918178'
      '9918178'
        """)
    @Sql(scripts = {RESET_DATABASE, ADD_SUBMIT_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnEmptyCrownFlagWhenJohAttributesIsMissingOrNotEffective(String personalCode) {
        Optional<JudicialOfficeHolder> judicialOfficeHolder
            = judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributesFilteredByEffectiveStartDate(
            personalCode,
            LocalDate.now());

        assertThat(judicialOfficeHolder.stream())
            .map(JudicialOfficeHolder::getJohAttributes)
            .isEmpty();
    }
}

