package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.AbstractTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.JpsRole;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.DELETE_SITTING_RECORD_STATUS_HISTORY;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class SittingRecordRepositoryTest extends AbstractTest {


    @Autowired
    private SittingRecordRepository recordRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    private StatusHistory statusHistoryRecorded;

    private static final String PERSONAL_CODE = "001";

    @BeforeEach
    void setUp() {
        judicialOfficeHolderRepository.deleteAll();
        recordRepository.deleteAll();
        historyRepository.deleteAll();
    }

    @Test
    void shouldSaveSittingRecord() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2), PERSONAL_CODE);
        StatusHistory statusHistoryRecorded1 = createStatusHistory(sittingRecord.getStatusId(),
                                                   JpsRole.ROLE_RECORDER.name(),
                                                   "John Doe",
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded1);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertThat(persistedSittingRecord).isEqualTo(sittingRecord);
    }

    @Test
    void shouldUpdateSittingRecordWhenRecordIsPresent() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2), PERSONAL_CODE);
        StatusHistory statusHistoryRecorded1 = createStatusHistory(sittingRecord.getStatusId(),
                                                   "555",
                                                   "John Doe 555",
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded1);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> optionalSettingRecordToUpdate = recordRepository
            .findById(persistedSittingRecord.getId());
        assertThat(optionalSettingRecordToUpdate).isPresent();

        SittingRecord settingRecordToUpdate = optionalSettingRecordToUpdate.get();
        settingRecordToUpdate.setSittingDate(LocalDate.now().minusDays(30));

        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(StatusId.SUBMITTED)
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(JpsRole.ROLE_SUBMITTER.getValue())
            .changedByName("John Doe")
            .sittingRecord(settingRecordToUpdate)
            .build();
        settingRecordToUpdate.addStatusHistory(statusHistory);

        SittingRecord updatedSittingRecord = recordRepository.save(settingRecordToUpdate);
        assertThat(updatedSittingRecord).isNotNull();
        assertThat(updatedSittingRecord).isEqualTo(settingRecordToUpdate);
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<SittingRecord> optionalSettingRecordToUpdate = recordRepository.findById(100L);
        assertThat(optionalSettingRecordToUpdate).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2), PERSONAL_CODE);
        StatusHistory statusHistoryRecorded1 = createStatusHistory(sittingRecord.getStatusId(),
                                                   JpsRole.ROLE_RECORDER.getValue(),
                                                   "John Doe",
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded1);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> optionalSettingRecordToUpdate = recordRepository
            .findById(persistedSittingRecord.getId());
        assertThat(optionalSettingRecordToUpdate).isPresent();

        SittingRecord settingRecordToDelete = optionalSettingRecordToUpdate.get();
        recordRepository.deleteById(settingRecordToDelete.getId());

        optionalSettingRecordToUpdate = recordRepository.findById(settingRecordToDelete.getId());
        assertThat(optionalSettingRecordToUpdate).isEmpty();
    }

    @Test
    void shouldFindCreatedByUserId() {
        SittingRecord sittingRecord = createSittingRecordWithSeveralStatus();

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        String createdByUserId = recordRepository.findCreatedByUserId(persistedSittingRecord.getId());

        assertNotNull(createdByUserId, "Could not find created by user id.");
        assertEquals(statusHistoryRecorded.getChangedByUserId(), createdByUserId,
                     "Not the expected CREATED BY USER ID!");
    }

    private SittingRecord createSittingRecordWithSeveralStatus() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2), PERSONAL_CODE);
        statusHistoryRecorded = createStatusHistory(sittingRecord.getStatusId(),
                                                    JpsRole.ROLE_RECORDER.getValue(),
                                                    "John Doe",
                                                    sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded);
        StatusHistory statusHistorySubmitted1 = createStatusHistory(StatusId.SUBMITTED,
                                                   JpsRole.ROLE_RECORDER.getValue(),
                                                   "Matthew Doe",
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistorySubmitted1);

        StatusHistory statusHistoryPublished = createStatusHistory(StatusId.PUBLISHED,
                                                     JpsRole.ROLE_RECORDER.getValue(),
                                                     "Mark Doe",
                                                     sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryPublished);

        return sittingRecord;
    }

    @Test
    void shouldReturnSittingRecordWithStatusHistoryOfRecorderWhenStatusIsNotDeleted() {
        SittingRecord sittingRecord = createSittingRecordWithSeveralStatus();

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> recorderSittingRecord = recordRepository.findRecorderSittingRecord(
            persistedSittingRecord.getId(),
            StatusId.DELETED
        );
        assertThat(recorderSittingRecord)
            .isPresent()
            .map(SittingRecord::getStatusId)
            .contains(StatusId.PUBLISHED);
        assertThat(recorderSittingRecord)
            .map(SittingRecord::getStatusHistories)
            .hasValue(persistedSittingRecord.getStatusHistories());
    }

    @Test
    void shouldNotReturnSittingRecordWithStatusHistoryOfRecorderWhenStatusIsDeleted() {
        SittingRecord sittingRecord = createSittingRecordWithSeveralStatus();
        StatusHistory statusHistory = createStatusHistory(
            StatusId.DELETED,
            JpsRole.ROLE_RECORDER.name(),
            "John Doe",
            sittingRecord
        );
        sittingRecord.addStatusHistory(statusHistory);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> recorderSittingRecord = recordRepository.findRecorderSittingRecord(
            persistedSittingRecord.getId(),
            StatusId.DELETED
        );
        assertThat(recorderSittingRecord).isEmpty();
    }

    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldReturnRecordsToBeSubmittedWhenMatchRecordFoundInSittingRecordsTable() {
        SubmitSittingRecordRequest submitSittingRecordRequest = SubmitSittingRecordRequest.builder()
            .regionId("4")
            .dateRangeFrom(LocalDate.parse("2023-05-11"))
            .dateRangeTo(LocalDate.parse("2023-05-11"))
            .createdByUserId("d139a314-eb40-45f4-9e7a-9e13f143cc3a")
            .build();


        List<Long> recordsToSubmit = recordRepository.findRecordsToSubmit(
            submitSittingRecordRequest,
            "BBA3"
        );
        assertThat(recordsToSubmit)
            .hasSize(4)
            .contains(2L, 3L, 5L, 6L);
    }
}
