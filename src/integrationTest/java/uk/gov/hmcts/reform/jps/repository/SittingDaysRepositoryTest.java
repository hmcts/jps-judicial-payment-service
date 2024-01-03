package uk.gov.hmcts.reform.jps.repository;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.SittingDays;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_PUBLISHED_TEST_DATA;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;


@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class SittingDaysRepositoryTest {

    @Autowired
    private SittingDaysRepository sittingDaysRepository;

    @Test
    @Sql(RESET_DATABASE)
    void shouldSaveSittingDays() {
        SittingDays persistedSittingDays = getSittingDaysPersisted();

        assertThat(persistedSittingDays.getId())
            .isEqualTo(1L);
    }

    @NotNull
    private SittingDays getSittingDaysPersisted() {
        SittingDays sittingDays = SittingDays.builder()
            .personalCode("123")
            .judgeRoleTypeId("judge")
            .financialYear("2023-24")
            .sittingCount(300L)
            .build();

        return sittingDaysRepository.save(sittingDays);
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<SittingDays> optionalSittingDays = sittingDaysRepository.findById(100L);
        assertThat(optionalSittingDays).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        SittingDays persistedSittingDays = getSittingDaysPersisted();

        sittingDaysRepository.deleteById(persistedSittingDays.getId());
        Optional<SittingDays> optionalSittingDays = sittingDaysRepository
            .findById(persistedSittingDays.getId());

        assertThat(optionalSittingDays).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
      # PERSONALCODE, COUNT
        4918178,      300
        555555,       0
        """)
    @Sql(scripts = {RESET_DATABASE, INSERT_PUBLISHED_TEST_DATA})
    void shouldSittingCountWhenPersonalCodeIsPresentForFinalYear(String personalCode, Long count) {
        Long sittingCountByPersonalCodeAndFinancialYear = sittingDaysRepository
            .findSittingCountByPersonalCodeAndFinancialYear(
                personalCode,
                "2023-24"
            ).orElse(0L);
        assertThat(sittingCountByPersonalCodeAndFinancialYear)
            .isEqualTo(count);
    }

    @Test
    void shouldUpdateSittingCount() {
        SittingDays persistedSittingDays = getSittingDaysPersisted();
        Long sittingDaysCount = 117L;

        sittingDaysRepository.updateSittingCount(sittingDaysCount, persistedSittingDays.getJudgeRoleTypeId(),
                                                 persistedSittingDays.getPersonalCode(),
                                                 persistedSittingDays.getFinancialYear());
        Optional<SittingDays> optionalSittingDays = sittingDaysRepository.findById(persistedSittingDays.getId());

        assertEquals(sittingDaysCount, optionalSittingDays.get().getSittingCount());
    }

}
