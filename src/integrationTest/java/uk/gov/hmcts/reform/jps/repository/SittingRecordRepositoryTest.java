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
class SittingRecordRepositoryTest extends AbstractTest {

    @Autowired
    private SittingRecordRepository recordRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;
    private StatusHistory statusHistoryRecorded;

    @BeforeEach
    void setUp() {
        recordRepository.deleteAll();
        historyRepository.deleteAll();
    }

    @Test
    void shouldSaveSittingRecord() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        StatusHistory statusHistoryCreated = createStatusHistory(sittingRecord.getStatusId(),
                                                   LocalDateTime.now(),
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
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
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
            .statusId(StatusId.SUBMITTED.name())
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(JpsRole.ROLE_SUBMITTER.getValue())
            .changeByName("John Doe")
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
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
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
        assertEquals(statusHistoryRecorded.getChangeByUserId(), createdByUserId,
                     "Not the expected CREATED BY USER ID!");
    }

    private SittingRecord createSittingRecordWithSeveralStatus() {
        SittingRecord sittingRecord = createSittingRecord(LocalDate.now().minusDays(2));
        statusHistoryRecorded = createStatusHistory(sittingRecord.getStatusId(),
                                                    JpsRole.ROLE_RECORDER.getValue(),
                                                    "John Doe",
                                                    sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryRecorded);
        StatusHistory statusHistorySubmitted1 = createStatusHistory(StatusId.SUBMITTED.name(),
                                                   JpsRole.ROLE_RECORDER.getValue(),
                                                   "Matthew Doe",
                                                   sittingRecord);
        sittingRecord.addStatusHistory(statusHistorySubmitted1);

        StatusHistory statusHistoryPublished = createStatusHistory(StatusId.PUBLISHED.name(),
                                                     JpsRole.ROLE_RECORDER.getValue(),
                                                     "Mark Doe",
                                                     sittingRecord);
        sittingRecord.addStatusHistory(statusHistoryPublished);

        return sittingRecord;
    }
}
