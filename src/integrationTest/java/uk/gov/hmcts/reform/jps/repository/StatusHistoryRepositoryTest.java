package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class StatusHistoryRepositoryTest {

    @Autowired
    private StatusHistoryRepository historyRepository;

    @Test
    void shouldSaveStatusHistory() {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusID("recorded")
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .build();

        StatusHistory persistedStatusHistory = historyRepository.save(statusHistory);
        assertThat(persistedStatusHistory).isNotNull();
        assertThat(persistedStatusHistory.getId()).isNotNull();
        assertThat(persistedStatusHistory).isEqualTo(statusHistory);
    }

    @Test
    void shouldUpdateStatusHistoryWhenRecordIsPresent() {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusID("recorded")
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .build();

        StatusHistory persistedStatusHistory = historyRepository.save(statusHistory);

        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository
            .findById(persistedStatusHistory.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        StatusHistory settingHistoryToUpdate = optionalSettingHistoryToUpdate.get();
        settingHistoryToUpdate.setChangeDateTime(LocalDateTime.now());
        settingHistoryToUpdate.setChangeByUserId("jp-submitter");

        StatusHistory updatedStatusHistory = historyRepository.save(settingHistoryToUpdate);
        assertThat(updatedStatusHistory).isNotNull();
        assertThat(updatedStatusHistory).isEqualTo(settingHistoryToUpdate);
    }

    @Test
    void shouldReturnEmptyWhenHistorydNotFound() {
        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository.findById(100L);
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    void shouldDeleteSelectedHistory() {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusID("recorded")
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .build();

        StatusHistory persistedStatusHistory = historyRepository.save(statusHistory);

        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository
            .findById(persistedStatusHistory.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        StatusHistory settingHistoryToDelete = optionalSettingHistoryToUpdate.get();
        historyRepository.deleteById(settingHistoryToDelete.getId());

        optionalSettingHistoryToUpdate = historyRepository.findById(settingHistoryToDelete.getId());
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

}

