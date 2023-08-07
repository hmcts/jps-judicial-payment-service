package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.AbstractTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.JpsRole;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class StatusHistoryRepositoryTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHistoryRepositoryTest.class);

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
        historyRepository.deleteAll();
        recordRepository.deleteAll();
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        statusHistoryRecorded = createStatusHistory(
            sittingRecord.getStatusId(),
            "john_doe",
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
            settingHistoryToUpdate.setChangedDateTime(LocalDateTime.now());
            settingHistoryToUpdate.setChangedByUserId(JpsRole.ROLE_SUBMITTER.name());
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

        statusHistorySubmitted = createStatusHistory(StatusId.SUBMITTED,
            JpsRole.ROLE_RECORDER.name(),
            "Matthew Doe",
            persistedSittingRecord);
        persistedSittingRecord.addStatusHistory(statusHistorySubmitted);
        historyRepository.save(statusHistorySubmitted);
        persistedSittingRecord = recordRepository.save(persistedSittingRecord);

        StatusHistory statusHistoryPublished = createStatusHistory(StatusId.PUBLISHED,
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
        assertEquals(statusHistoryFound, statusHistoryRecorded, "Not the expected " + StatusId.SUBMITTED
            + " status history!");
    }

    @Test
    void shouldFindLatestStatus() {

        statusHistorySubmitted = createStatusHistory(StatusId.SUBMITTED,
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
        assertEquals(statusHistoryFound, statusHistorySubmitted, "Not the expected " + StatusId.SUBMITTED
            + " status history!");
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
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindRecordingUsersGivenCriteria(SSC_ID, null, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButRegionId2() {
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, "2", statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButHmctsServiceIdId2() {
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria("ssc_id2", REGION_ID, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButDateRange() {
        List<String> statusIds = Arrays.asList(StatusId.RECORDED.name(), StatusId.PUBLISHED.name(),
                                               StatusId.SUBMITTED.name()
        );
        LocalDate startDate = LocalDate.now().minusDays(100);
        LocalDate endDate = LocalDate.now().minusDays(50);

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, REGION_ID, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButDeleted() {
        List<String> statusIds = Arrays.asList(StatusId.DELETED.name());
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, REGION_ID, statusIds, startDate, endDate);
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
        recordingUsers.forEach(e ->
                       assertTrue(e.getUserId().contains("john_")));
    }

    private void shouldFindNoRecordingUsersGivenCriteria(String hmctsServiceId, String regionId, List<String> statusIds,
                                                         LocalDate startDate, LocalDate endDate) {
        createThreeBatches();

        List<RecordingUser> recordingUsers = historyRepository
            .findRecordingUsers(hmctsServiceId, regionId, statusIds, startDate, endDate)
            .stream().sorted().toList();
        recordingUsers.forEach(e ->
            LOGGER.info("recordingUser:{}:{}", e.getUserId(), e.getUserName()));


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
        sittingRecord = updateSittingRecordToSubmitted(sittingRecord, "matt_doe", "Matthew Doe"
        );
        sittingRecords.add(sittingRecord);

        SittingRecord sittingRecord2 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_smith", "John Smith"
        );
        sittingRecord2 = updateSittingRecordToSubmitted(sittingRecord2, "matt_smith", "Matthew Smith"
        );
        sittingRecord2 = updateSittingRecordToPublished(sittingRecord2, "matt_smith", "Matthew Smith"
        );
        sittingRecords.add(sittingRecord2);

        SittingRecord sittingRecord3 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_jones", "John Jones"
        );
        sittingRecord3 = updateSittingRecordToSubmitted(sittingRecord3, "matt_jones", "Matthew Jones"
        );
        sittingRecords.add(sittingRecord3);

        SittingRecord sittingRecord4 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_james", "John James"
        );
        sittingRecord4 = updateSittingRecordToPublished(sittingRecord4, "steve_james", "Steve James"
        );
        sittingRecords.add(sittingRecord4);

        return sittingRecords;
    }

    private SittingRecord createNewSittingRecord(LocalDate localDate, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(localDate);
        StatusHistory statusHistoryCreated1 = createStatusHistory(
            sittingRecord.getStatusId(),
            userId,
            userName,
            sittingRecord
        );
        sittingRecord.addStatusHistory(statusHistoryCreated1);
        return recordRepository.save(sittingRecord);
    }

    private SittingRecord updateSittingRecordToSubmitted(SittingRecord sittingRecord, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.SUBMITTED.name(), userId, userName);
    }

    private SittingRecord updateSittingRecordToPublished(SittingRecord sittingRecord, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.PUBLISHED.name(), userId, userName
        );
    }

    private SittingRecord updateSittingRecord(SittingRecord sittingRecord, String statusId, String userId,
                                              String userName) {
        StatusHistory statusHistory = createStatusHistory(
            statusId,
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
    @Test
    void shouldReturnLastRecordedStatusHistoryWhenMultipleRecordsPresentForASittingRecord() {
        SittingRecord sittingRecord = createSittingRecord();
        Arrays.stream(StatusId.values())
            .map(this::createStatusHistory)
            .forEach(sittingRecord::addStatusHistory);

        SittingRecord savedSittingRecord = recordRepository.save(sittingRecord);

        List<StatusHistory> statusHistories = historyRepository.findAll();
        Optional<StatusHistory> latestSavedStatusHistory = statusHistories.stream()
            .max(Comparator.comparing(StatusHistory::getChangeDateTime));

        Sort.TypedSort<StatusHistory> sort = Sort.sort(StatusHistory.class);
        Optional<StatusHistory> lastStatusHistory = historyRepository.findFirstBySittingRecord(
            SittingRecord.builder()
                .id(savedSittingRecord.getId())
                .build(),
            sort.by(StatusHistory::getId).descending()
        );

        assertThat(lastStatusHistory).isPresent();
        assertThat(lastStatusHistory).hasValue(latestSavedStatusHistory.get());
    }

    SittingRecord createSittingRecord() {
        return SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(RECORDED)
            .regionId("1")
            .epimmsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .build();
    }

    StatusHistory createStatusHistory(StatusId statusId) {
        return  StatusHistory.builder()
            .statusId(statusId)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .changeByName("John Doe")
            .build();
    }
}

