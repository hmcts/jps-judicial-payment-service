package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.Fee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_FEE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class FeeRepositoryTest {

    @Autowired
    private FeeRepository feeRepository;

    private Fee fee;

    private Fee persistedFee;

    private static final BigDecimal STANDARD_FEE = BigDecimal.valueOf(15000.65);
    private static final BigDecimal HIGHER_THRESHOLD_FEE = BigDecimal.valueOf(375000.99);
    private static final BigDecimal LONDON_WEIGHTED_FEE = BigDecimal.valueOf(1000.75);

    @BeforeEach
    public void setUp() {
        fee = createFee();
        persistedFee = feeRepository.save(fee);
    }

    @Test
    void shouldSaveFee() {
        assertThat(persistedFee).isNotNull();
        assertThat(persistedFee.getId()).isNotNull();
        assertThat(persistedFee).isEqualTo(fee);
    }

    @Test
    void shouldReturnWhenFeeExists() {
        Optional<Fee> optionalFoundFee = feeRepository.findById(persistedFee.getId());
        assertSame(persistedFee, optionalFoundFee.get());
        assertTrue(STANDARD_FEE.equals(persistedFee.getStandardFee()));
        assertTrue(LONDON_WEIGHTED_FEE.equals(persistedFee.getLondonWeightedFee()));
        assertTrue(HIGHER_THRESHOLD_FEE.equals(persistedFee.getHigherThresholdFee()));
    }

    @Test
    void shouldReturnEmptyWhenFeeNotFound() {
        Optional<Fee> optionalSettingHistoryToUpdate = feeRepository.findById(100L);
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    void shouldDeleteSelectedFee() {
        Optional<Fee> optionalSettingHistoryToUpdate = feeRepository.findById(persistedFee.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        Fee settingHistoryToDelete = optionalSettingHistoryToUpdate.get();
        feeRepository.deleteById(settingHistoryToDelete.getId());

        optionalSettingHistoryToUpdate = feeRepository.findById(settingHistoryToDelete.getId());
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_FEE})
    void shouldDeleteRecordsWhenIdsPassed() {
        List<Long> courtVenueIds = feeRepository.findAll().stream()
            .map(Fee::getId)
            .toList();
        assertThat(courtVenueIds).isNotEmpty();
        feeRepository.deleteByIds(courtVenueIds);
        List<Fee> courtVenues = feeRepository.findAll();
        assertThat(courtVenues).isEmpty();
    }

    private Fee createFee() {
        return Fee.builder()
            .hmctsServiceId("123")
            .judgeRoleId("2")
            .standardFee(STANDARD_FEE)
            .higherThresholdFee(HIGHER_THRESHOLD_FEE)
            .londonWeightedFee(LONDON_WEIGHTED_FEE)
            .effectiveFrom(LocalDate.now().minusDays(2))
            .feeCreatedDate(LocalDate.now())
            .build();
    }

}
