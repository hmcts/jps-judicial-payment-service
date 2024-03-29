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
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.AbstractTest;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.JpsRole;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class StatusHistoryRepositoryTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHistoryRepositoryTest.class);

    @Autowired
    private StatusHistoryRepository historyRepository;
    @Autowired
    private JudicialOfficeHolderRepository johRepository;
    @Autowired
    private SittingRecordRepository recordRepository;

    private SittingRecord persistedSittingRecord;
    private StatusHistory persistedStatusHistoryRecorded;
    private StatusHistory statusHistoryRecorded;
    private StatusHistory statusHistorySubmitted;

    private static final String PERSONAL_CODE = "001";

    @BeforeEach
    public void setUp() {
        johRepository.deleteAll();
        historyRepository.deleteAll();
        recordRepository.deleteAll();

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode("001")
            .build();
        johRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2), PERSONAL_CODE);
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
        Optional<StatusHistory> firstStatusHistory = persistedSittingRecord.getFirstStatusHistory();
        assertThat(firstStatusHistory)
            .isPresent()
            .map(StatusHistory::getStatusId)
            .hasValue(StatusId.RECORDED);

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
        Optional<StatusHistory> latestStatusHistory = persistedSittingRecord.getLatestStatusHistory();
        assertThat(latestStatusHistory)
            .isPresent()
            .map(StatusHistory::getStatusId)
            .hasValue(StatusId.SUBMITTED);


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
        List<StatusId> statusIds = Arrays.asList(StatusId.RECORDED, StatusId.PUBLISHED,
                                               StatusId.SUBMITTED
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindRecordingUsersGivenCriteria(hmctsServiceId, regionId, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindAllRecordingUsersForAllCriteriaButNullRegionId() {
        List<StatusId> statusIds = Arrays.asList(StatusId.RECORDED, StatusId.PUBLISHED,
                                               StatusId.SUBMITTED
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindRecordingUsersGivenCriteria(SSC_ID, null, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButRegionId2() {
        List<StatusId> statusIds = Arrays.asList(StatusId.RECORDED, StatusId.PUBLISHED,
                                               StatusId.SUBMITTED
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, "2", statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButHmctsServiceIdId2() {
        List<StatusId> statusIds = Arrays.asList(StatusId.RECORDED, StatusId.PUBLISHED,
                                               StatusId.SUBMITTED
        );
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria("ssc_id2", REGION_ID, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButDateRange() {
        List<StatusId> statusIds = Arrays.asList(StatusId.RECORDED, StatusId.PUBLISHED,
                                               StatusId.SUBMITTED
        );
        LocalDate startDate = LocalDate.now().minusDays(100);
        LocalDate endDate = LocalDate.now().minusDays(50);

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, REGION_ID, statusIds, startDate, endDate);
    }

    @Test
    void shouldFindNoRecordingUsersForAllCriteriaButDeleted() {
        List<StatusId> statusIds = Arrays.asList(StatusId.DELETED);
        LocalDate startDate = LocalDate.now().minusDays(50);
        LocalDate endDate = LocalDate.now();

        shouldFindNoRecordingUsersGivenCriteria(SSC_ID, REGION_ID, statusIds, startDate, endDate);
    }

    private void shouldFindRecordingUsersGivenCriteria(String hmctsServiceId, String regionId, List<StatusId> statusIds,
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

    private void shouldFindNoRecordingUsersGivenCriteria(
        String hmctsServiceId,
        String regionId,
        List<StatusId> statusIds,
        LocalDate startDate,
        LocalDate endDate) {

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
                                                             "john_doe", "John Doe");
        sittingRecord = updateSittingRecordToSubmitted(sittingRecord, "matt_doe", "Matthew Doe");
        sittingRecords.add(sittingRecord);

        SittingRecord sittingRecord2 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_smith", "John Smith");
        sittingRecord2 = updateSittingRecordToSubmitted(sittingRecord2, "matt_smith", "Matthew Smith");
        sittingRecord2 = updateSittingRecordToPublished(sittingRecord2, "matt_smith", "Matthew Smith");
        sittingRecords.add(sittingRecord2);

        SittingRecord sittingRecord3 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_jones", "John Jones");
        sittingRecord3 = updateSittingRecordToSubmitted(sittingRecord3, "matt_jones", "Matthew Jones");
        sittingRecords.add(sittingRecord3);

        SittingRecord sittingRecord4 = createNewSittingRecord(LocalDate.now().minusDays(2),
                                                              "john_james", "John James");
        sittingRecord4 = updateSittingRecordToPublished(sittingRecord4, "steve_james", "Steve James");
        sittingRecords.add(sittingRecord4);

        return sittingRecords;
    }

    private SittingRecord createNewSittingRecord(LocalDate localDate, String userId, String userName) {
        SittingRecord sittingRecord = createSittingRecord(localDate, PERSONAL_CODE);
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
        return updateSittingRecord(sittingRecord, StatusId.SUBMITTED, userId, userName);
    }

    private SittingRecord updateSittingRecordToPublished(SittingRecord sittingRecord, String userId, String userName) {
        return updateSittingRecord(sittingRecord, StatusId.PUBLISHED, userId, userName);
    }

    private SittingRecord updateSittingRecord(SittingRecord sittingRecord, StatusId statusId, String userId,
                                              String userName) {
        StatusHistory statusHistory = createStatusHistory(
            statusId,
            userId,
            userName,
            sittingRecord
        );
        StatusHistory persistedStatusHistory = historyRepository.save(statusHistory);
        sittingRecord.addStatusHistory(persistedStatusHistory);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord.getLatestStatusHistory())
            .isPresent()
            .map(StatusHistory::getStatusId)
            .hasValue(statusId);
        return persistedSittingRecord;
    }

    @Test
    void shouldReturnLastRecordedStatusHistoryWhenMultipleRecordsPresentForASittingRecord() {
        SittingRecord sittingRecord =  createSittingRecord(
            LocalDate.now().minusDays(2),
            "001"
        );
        Arrays.stream(StatusId.values())
            .map(statusId -> createStatusHistory(
                statusId,
                "jp-recorder",
                "John Doe",
                sittingRecord
            ))
            .forEach(sittingRecord::addStatusHistory);

        SittingRecord savedSittingRecord = recordRepository.save(sittingRecord);

        List<StatusHistory> statusHistories = historyRepository.findAll();
        Optional<StatusHistory> latestSavedStatusHistory = statusHistories.stream()
            .max(Comparator.comparing(StatusHistory::getChangedDateTime));

        Sort.TypedSort<StatusHistory> sort = Sort.sort(StatusHistory.class);
        Optional<StatusHistory> lastStatusHistory = historyRepository.findFirstBySittingRecord(
            SittingRecord.builder()
                .id(savedSittingRecord.getId())
                .build(),
            sort.by(StatusHistory::getId).descending()
        );

        assertThat(lastStatusHistory)
            .isPresent()
            .hasValue(latestSavedStatusHistory.get());
    }

    @Test
    @Sql(scripts = RESET_DATABASE)
    void shouldReturnFirstRecordedStatusHistoryWhenMultipleRecordsPresentForASittingRecord() {
        SittingRecord sittingRecord =  createSittingRecord(
            LocalDate.now().minusDays(2),
            "001"
        );
        Arrays.stream(StatusId.values())
            .map(statusId -> createStatusHistory(
                statusId,
                "jp-recorder",
                "John Doe",
                sittingRecord
                ))
            .forEach(sittingRecord::addStatusHistory);

        SittingRecord savedSittingRecord = recordRepository.save(sittingRecord);

        List<StatusHistory> statusHistories = historyRepository.findAll();
        Optional<StatusHistory> firstSavedStatusHistory = statusHistories.stream()
            .filter(statusHistory -> statusHistory.getSittingRecord().getId().equals(sittingRecord.getId()))
            .min(Comparator.comparing(StatusHistory::getChangedDateTime));

        Optional<StatusHistory> firstStatusHistory = historyRepository.findBySittingRecordAndStatusId(
            SittingRecord.builder()
                .id(savedSittingRecord.getId())
                .build(),
            RECORDED
        );

        assertThat(firstStatusHistory)
            .isPresent()
            .hasValue(firstSavedStatusHistory.get());
    }
}

