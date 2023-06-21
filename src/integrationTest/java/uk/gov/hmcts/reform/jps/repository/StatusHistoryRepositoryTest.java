package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.AbstractTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.JpsRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class StatusHistoryRepositoryTest extends AbstractTest {

    @Autowired
    private StatusHistoryRepository historyRepository;
    @Autowired
    private SittingRecordRepository recordRepository;

    private SittingRecord persistedSittingRecord;
    private StatusHistory persistedStatusHistoryCreated;
    private StatusHistory persistedStatusHistoryAmended;
    private StatusHistory statusHistoryCreated;
    private StatusHistory statusHistoryAmended;


    @BeforeEach
    public void setUp() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        statusHistoryCreated = createStatusHistory(sittingRecord.getStatusId(),
                                                          JpsRole.ROLE_RECORDER.name(),
                                                          "John Doe",
                                                            sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryCreated);
        persistedSittingRecord = recordRepository.save(sittingRecord);
        persistedStatusHistoryCreated = persistedSittingRecord.getStatusHistories().get(0);
    }

    @Test
    void shouldSaveStatusHistory() {

        assertThat(persistedStatusHistoryCreated).isNotNull();
        assertThat(persistedStatusHistoryCreated.getId()).isNotNull();
        assertThat(persistedStatusHistoryCreated).isEqualTo(statusHistoryCreated);
    }

    @Test
    void shouldUpdateStatusHistoryWhenRecordIsPresent() {

        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository
            .findById(persistedStatusHistoryCreated.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        StatusHistory settingHistoryToUpdate = null;
        if (optionalSettingHistoryToUpdate.isPresent()) {
            settingHistoryToUpdate = optionalSettingHistoryToUpdate.get();
            settingHistoryToUpdate.setChangeDateTime(LocalDateTime.now());
            settingHistoryToUpdate.setChangeByUserId(JpsRole.ROLE_SUBMITTER.name());
        }

        StatusHistory updatedStatusHistory = historyRepository.save(settingHistoryToUpdate);
        assertThat(updatedStatusHistory).isNotNull();
        assertThat(updatedStatusHistory).isEqualTo(settingHistoryToUpdate);
    }

    @Test
    void shouldReturnEmptyWhenHistoryNotFound() {
        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository.findById(100L);
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    void shouldDeleteSelectedHistory() {

        Optional<StatusHistory> optionalSettingHistoryToUpdate =
            historyRepository.findById(persistedStatusHistoryCreated.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        StatusHistory settingHistoryToDelete = null;
        if (optionalSettingHistoryToUpdate.isPresent()) {
            settingHistoryToDelete = optionalSettingHistoryToUpdate.get();
            if (null != settingHistoryToDelete && null != settingHistoryToDelete.getId()) {
                historyRepository.deleteById(settingHistoryToDelete.getId());
            }
        }

        optionalSettingHistoryToUpdate = historyRepository.findById(settingHistoryToDelete.getId());
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
    }

    @Test
    void shouldFindCreatedStatus() {

        statusHistoryAmended = createStatusHistory("amended",
                                                   JpsRole.ROLE_RECORDER.name(),
                                                   "Matthew Doe",
                                                                 persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistoryAmended);
        persistedStatusHistoryAmended = historyRepository.save(statusHistoryAmended);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);
        persistedStatusHistoryCreated = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryAsc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistoryCreated, "Not the expected CREATED status history!");
    }

    @Test
    void shouldFindLatestStatus() {

        statusHistoryAmended = createStatusHistory("amended",
                                                                 JpsRole.ROLE_RECORDER.name(),
                                                                 "Matthew Doe",
                                                                 persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistoryAmended);
        persistedStatusHistoryAmended = historyRepository.save(statusHistoryAmended);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);
        persistedStatusHistoryCreated = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryDesc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistoryAmended, "Not the expected AMENDED status history!");
    }

}

