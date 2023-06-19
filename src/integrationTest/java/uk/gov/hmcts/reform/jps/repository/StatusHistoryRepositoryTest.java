package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class StatusHistoryRepositoryTest {

    @Autowired
    private StatusHistoryRepository historyRepository;
    @Autowired
    private SittingRecordRepository recordRepository;

    private StatusHistory persistedStatusHistory;

    private StatusHistory statusHistory;

    @BeforeEach
    public void setUp() {
        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(RECORDED)
            .regionId("1")
            .epimmsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("jp-recorder")
            .build();

        statusHistory = StatusHistory.builder()
            .statusId(RECORDED)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .changeByName("John Doe")
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        persistedStatusHistory = persistedSittingRecord.getStatusHistories().get(0);
    }

    @Test
    void shouldSaveStatusHistory() {
        assertThat(persistedStatusHistory).isNotNull();
        assertThat(persistedStatusHistory.getId()).isNotNull();
        assertThat(persistedStatusHistory).isEqualTo(statusHistory);
    }

    @Test
    void shouldUpdateStatusHistoryWhenRecordIsPresent() {

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

        Optional<StatusHistory> optionalSettingHistoryToUpdate = historyRepository
            .findById(persistedStatusHistory.getId());
        assertThat(optionalSettingHistoryToUpdate).isPresent();

        StatusHistory settingHistoryToDelete = optionalSettingHistoryToUpdate.get();
        historyRepository.deleteById(settingHistoryToDelete.getId());

        optionalSettingHistoryToUpdate = historyRepository.findById(settingHistoryToDelete.getId());
        assertThat(optionalSettingHistoryToUpdate).isEmpty();
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
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("jp-recorder")
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

