package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.Fee;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class StatusHistoryRepositoryTest {

    @Autowired
    private FeeRepository feeRepository;

    private Fee fee;

    private Fee persistedFee;


    @BeforeEach
    public void setUp() {
        Fee fee = Fee.builder()
            .hmctsServiceId("123")
            .feeId("1")
            .judgeRoleId("2")
            .standardFee(5)
            .londonWeightedFee(1)
            .feeDescription("fee")
            .effectiveFrom(LocalDate.now().minusDays(2))
            .pensionableCode(1)
            .build();
        Fee persistedFee = feeRepository.save(fee);

    }

    @Test
    void shouldSaveFee() {

        assertThat(persistedFee).isNotNull();
        assertThat(persistedFee.getId()).isNotNull();
        assertThat(persistedFee).isEqualTo(fee);
    }


    @Test
    void shouldReturnEmptyWhenFeeNotFound() {
        Optional<Fee> optionalSettingHistoryToUpdate = feeRepository.findById(100L);
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    void shouldDeleteSelectedFeey() {

        Optional<Fee> optionalSettingHistoryToUpdate = feeRepository
            .findById(persistedFee.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        Fee settingHistoryToDelete = optionalSettingHistoryToUpdate.get();
        feeRepository.deleteById(settingHistoryToDelete.getId());

        optionalSettingHistoryToUpdate = feeRepository.findById(settingHistoryToDelete.getId());
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

}
