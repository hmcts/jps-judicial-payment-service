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
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                                                          "john_doe",
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
                                                                 "matt_doe",
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

    @Test
    void shouldFindAllRecordingUsers() {
        createBatchTestData();
        createBatchTestData();
        createBatchTestData();

        String hmctsServiceId = "ssc_id";
        String regionId = "1";
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name());
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();


        List<RecordingUser> recordingUsers = historyRepository
            .findRecordingUsers(hmctsServiceId, regionId,statusIds,startDate, endDate)
            .stream().sorted().toList();

        assertFalse(recordingUsers.isEmpty());
        assertEquals(recordingUsers.size(), 4);
        recordingUsers.stream().forEach(e ->
            assertTrue(e.getChangeByUserId().contains("john_")));

    }

    private List<SittingRecord> createBatchTestData() {
        List<SittingRecord> sittingRecords = new ArrayList<>();

        SittingRecord sittingRecord = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                             "john_doe","John Doe");
        sittingRecord = updateSittingRecordToSubmitted(sittingRecord, LocalDate.now().minusDays(2),
                                                             "matt_doe","Matthew Doe");
        sittingRecords.add(sittingRecord);

        SittingRecord sittingRecord2 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                             "john_smith","John Smith");
        sittingRecord2 = updateSittingRecordToSubmitted(sittingRecord2, LocalDate.now().minusDays(2),
                                                       "matt_smith","Matthew Smith");
        sittingRecord2 = updateSittingRecordToPublished(sittingRecord2, LocalDate.now().minusDays(2),
                                                        "matt_smith","Matthew Smith");
        sittingRecords.add(sittingRecord2);


        SittingRecord sittingRecord3 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_jones","John Jones");
        sittingRecord3 = updateSittingRecordToSubmitted(sittingRecord3, LocalDate.now().minusDays(2),
                                                        "matt_jones","Matthew Jones");
        sittingRecords.add(sittingRecord3);


        SittingRecord sittingRecord4 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_james","John James");
        sittingRecord4 = updateSittingRecordToPublished(sittingRecord4, LocalDate.now().minusDays(2),
                                                      "steve_james","Steve James");
        sittingRecords.add(sittingRecord4);

        return sittingRecords;
    }

    private SittingRecord createNewSittingRecord(LocalDate localDate, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(localDate);
        StatusHistory statusHistoryCreated = createStatusHistory(sittingRecord.getStatusId(),
                                                   userId,
                                                   userName,
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryCreated);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        StatusHistory persistedStatusHistoryCreated = persistedSittingRecord.getStatusHistories().get(0);
        return persistedSittingRecord;
    }

    private SittingRecord updateSittingRecordToSubmitted(SittingRecord sittingRecord,
                                                         LocalDate localDate, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.SUBMITTED.name(),
                                                           localDate, userId, userName);
    }

    private SittingRecord updateSittingRecordToPublished(SittingRecord sittingRecord,
                                                         LocalDate localDate, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.PUBLISHED.name(),
                                   localDate, userId, userName);
    }


    private SittingRecord updateSittingRecord(SittingRecord sittingRecord, String statusId,
                                                         LocalDate localDate, String userId, String userName) {
        StatusHistory statusHistory = createStatusHistory(statusId, userId, userName,
                                                                    sittingRecord);
        StatusHistory persistedStatusHistory = historyRepository.save(statusHistory);
        sittingRecord.addStatusHistory(persistedStatusHistory);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        persistedStatusHistory = persistedSittingRecord.getStatusHistories()
            .get(persistedSittingRecord.getStatusHistories().size() - 1);
        assertEquals(statusId, persistedStatusHistory.getStatusId());
        return persistedSittingRecord;
    }

}

