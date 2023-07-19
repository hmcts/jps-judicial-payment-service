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
import uk.gov.hmcts.reform.jps.model.StatusId;

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
    private StatusHistory persistedStatusHistoryRecorded;
    private StatusHistory statusHistoryRecorded;
    private StatusHistory statusHistorySubmitted;

    @BeforeEach
    public void setUp() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        statusHistoryRecorded = createStatusHistory(sittingRecord.getStatusId(),
                                                    JpsRole.ROLE_RECORDER.name(),
                                                    "John Doe",
                                                    sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded);
        persistedSittingRecord = recordRepository.save(sittingRecord);
        persistedStatusHistoryRecorded = persistedSittingRecord.getStatusHistories().get(0);
    }

    @Test
    void shouldSaveStatusHistory() {

        assertThat(persistedStatusHistoryRecorded).isNotNull();
        assertThat(persistedStatusHistoryRecorded.getId()).isNotNull();
        assertThat(persistedStatusHistoryRecorded).isEqualTo(statusHistoryRecorded);
    }

    @Test
    void shouldUpdateStatusHistoryWhenRecordIsPresent() {

        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository
            .findById(persistedStatusHistoryRecorded.getId());
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
            historyRepository.findById(persistedStatusHistoryRecorded.getId());
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

        statusHistorySubmitted = createStatusHistory(StatusId.SUBMITTED.name(),
            JpsRole.ROLE_RECORDER.name(),
            "Matthew Doe",
            persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistorySubmitted);
        historyRepository.save(statusHistorySubmitted);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);


        StatusHistory statusHistoryPublished = createStatusHistory(StatusId.PUBLISHED.name(),
                                                     JpsRole.ROLE_RECORDER.name(),
                                                     "Matthew Doe",
                                                     persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistoryPublished);
        historyRepository.save(statusHistoryPublished);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);

        persistedStatusHistoryRecorded = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryAsc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistoryRecorded, "Not the expected " + StatusId.SUBMITTED.name()
            + " status history!");
    }

    @Test
    void shouldFindLatestStatus() {

        statusHistorySubmitted = createStatusHistory(StatusId.SUBMITTED.name(),
                                                     JpsRole.ROLE_RECORDER.name(),
                                                     "Matthew Doe",
                                                     persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistorySubmitted);
        historyRepository.save(statusHistorySubmitted);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);
        persistedStatusHistoryRecorded = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryDesc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistorySubmitted, "Not the expected " + StatusId.SUBMITTED.name()
            + " status history!");
    }

}

