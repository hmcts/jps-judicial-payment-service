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
    private StatusHistory persistedStatusHistoryRecorded;
    private StatusHistory persistedStatusHistoryPublished;
    private StatusHistory statusHistoryRecorded;
    private StatusHistory statusHistoryPublished;

    @BeforeEach
    public void setUp() {
        historyRepository.deleteAll();
        recordRepository.deleteAll();
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        statusHistoryRecorded = createStatusHistory(
            sittingRecord.getStatusId(),
            LocalDateTime.now(),
            "john_doe",
            "John Doe",
            sittingRecord
        );
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
    void shouldDeleteSelectedStatusHistory() {

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

        statusHistoryPublished = createStatusHistory(
            "amended",
            LocalDateTime.now(),
            JpsRole.ROLE_RECORDER.name(),
            "Matthew Doe",
            persistedSittingRecord
        );
        persistedSittingRecord.addStatusHistory(statusHistoryPublished);
        persistedStatusHistoryPublished = historyRepository.save(statusHistoryPublished);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);
        persistedStatusHistoryRecorded = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryAsc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistoryRecorded, "Not the expected CREATED status history!");
    }

    @Test
    void shouldFindLatestStatus() {

        statusHistoryPublished = createStatusHistory(
            StatusId.PUBLISHED.name(),
            LocalDateTime.now(),
            "matt_doe",
            "Matthew Doe",
            persistedSittingRecord
        );
        persistedSittingRecord.addStatusHistory(statusHistoryPublished);
        persistedStatusHistoryPublished = historyRepository.save(statusHistoryPublished);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);
        persistedStatusHistoryRecorded = persistedSittingRecord.getFirstStatusHistory();

        StatusHistory statusHistoryFound = historyRepository
            .findStatusHistoryDesc(persistedSittingRecord.getId()).get(0);
        assertNotNull(statusHistoryFound, "Could not find any status history.");
        assertEquals(statusHistoryFound, statusHistoryPublished, "Not the expected AMENDED status history!");
    }

    @Test
    void shouldFindAllRecordingUsersForAllCriteria() {

        String hmctsServiceId = SSC_ID;
        String regionId = REGION_ID;
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindAllRecordingUsersForAllCriteriaButNullRegionId() {
        String hmctsServiceId = SSC_ID;
        String regionId = null;
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButRegionId2() {
        String hmctsServiceId = SSC_ID;
        String regionId = "2";
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButHmctsServiceIdId2() {
        String hmctsServiceId = "ssc_id2";
        String regionId = REGION_ID;
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButDateRange() {
        String hmctsServiceId = SSC_ID;
        String regionId = REGION_ID;
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(100);
        LocalDate endDate = LocalDate.now().minusDays(50);

        shouldFindNoRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButSubmitted() {
        String hmctsServiceId = SSC_ID;
        String regionId = REGION_ID;
        List<String> statusIds = Arrays.asList(StatusId.SUBMITTED.name());
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    private void shouldFindRecordingUsersGivenCriteria(String hmctsServiceId, String regionId, List<String> statusIds,
                                                       LocalDate startDate, LocalDate endDate) {
        createThreeBatches();

        List<RecordingUser> recordingUsers = historyRepository
            .findRecordingUsers(hmctsServiceId, regionId, statusIds, startDate, endDate)
            .stream().sorted().toList();

        assertFalse(recordingUsers.isEmpty());
        assertThat(recordingUsers).doesNotHaveDuplicates();
        assertEquals(recordingUsers.size(), 4);
        recordingUsers.stream().forEach(e ->
                                            assertTrue(e.getChangeByUserId().contains("john_")));
    }

    private void shouldFindNoRecordingUsersGivenCriteria(String hmctsServiceId, String regionId, List<String> statusIds,
                                                         LocalDate startDate, LocalDate endDate) {
        createThreeBatches();

        List<RecordingUser> recordingUsers = historyRepository
            .findRecordingUsers(hmctsServiceId, regionId, statusIds, startDate, endDate)
            .stream().sorted().toList();

        assertTrue(recordingUsers.isEmpty());
    }

    private void createThreeBatches() {
        createBatchTestData();  // 4 sitting recs
        createBatchTestData();  // 4 sitting recs
        createBatchTestData();  // 4 sitting recs
        List<SittingRecord> sittingRecordsAll = recordRepository.findAll();
        assertEquals(13, sittingRecordsAll.size());
    }

    private List<SittingRecord> createBatchTestData() {
        List<SittingRecord> sittingRecords = new ArrayList<>();

        SittingRecord sittingRecord = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                             "john_doe", "John Doe"
        );
        sittingRecord = updateSittingRecordToSubmitted(sittingRecord, LocalDate.now().minusDays(2),
                                                       "matt_doe", "Matthew Doe"
        );
        sittingRecords.add(sittingRecord);

        SittingRecord sittingRecord2 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_smith", "John Smith"
        );
        sittingRecord2 = updateSittingRecordToSubmitted(sittingRecord2, LocalDate.now().minusDays(2),
                                                        "matt_smith", "Matthew Smith"
        );
        sittingRecord2 = updateSittingRecordToPublished(sittingRecord2, LocalDate.now().minusDays(2),
                                                        "matt_smith", "Matthew Smith"
        );
        sittingRecords.add(sittingRecord2);

        SittingRecord sittingRecord3 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_jones", "John Jones"
        );
        sittingRecord3 = updateSittingRecordToSubmitted(sittingRecord3, LocalDate.now().minusDays(2),
                                                        "matt_jones", "Matthew Jones"
        );
        sittingRecords.add(sittingRecord3);

        SittingRecord sittingRecord4 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_james", "John James"
        );
        sittingRecord4 = updateSittingRecordToPublished(sittingRecord4, LocalDate.now().minusDays(2),
                                                        "steve_james", "Steve James"
        );
        sittingRecords.add(sittingRecord4);

        return sittingRecords;
    }

    private SittingRecord createNewSittingRecord(LocalDate localDate, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(localDate);
        StatusHistory statusHistoryCreated1 = createStatusHistory(
            sittingRecord.getStatusId(),
            LocalDateTime.now(),
            userId,
            userName,
            sittingRecord
        );
        sittingRecord.addStatusHistory(statusHistoryCreated1);
        SittingRecord persistedSittingRecord1 = recordRepository.save(sittingRecord);
        return persistedSittingRecord1;
    }

    private SittingRecord updateSittingRecordToSubmitted(SittingRecord sittingRecord,
                                                         LocalDate localDate, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.SUBMITTED.name(),
                                   localDate, userId, userName
        );
    }

    private SittingRecord updateSittingRecordToPublished(SittingRecord sittingRecord,
                                                         LocalDate localDate, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.PUBLISHED.name(),
                                   localDate, userId, userName
        );
    }

    private SittingRecord updateSittingRecord(SittingRecord sittingRecord, String statusId,
                                              LocalDate localDate, String userId, String userName) {
        StatusHistory statusHistory = createStatusHistory(
            statusId,
            LocalDateTime.now(),
            userId,
            userName,
            sittingRecord
        );
        StatusHistory persistedStatusHistory1 = historyRepository.save(statusHistory);
        sittingRecord.addStatusHistory(persistedStatusHistory1);

        SittingRecord persistedSittingRecord1 = recordRepository.save(sittingRecord);
        persistedStatusHistory1 = persistedSittingRecord1.getLatestStatusHistory();
        assertEquals(statusId, persistedStatusHistory1.getStatusId());
        return persistedSittingRecord;
    }

}

