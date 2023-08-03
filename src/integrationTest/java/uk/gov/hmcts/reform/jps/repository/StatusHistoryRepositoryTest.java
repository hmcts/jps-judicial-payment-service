package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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


        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode("001")
            .build();

        SittingRecord sittingRecord = SittingRecord.builder()
            .am(true)
            .contractTypeId(2L)
            .createdByUserId("jp-recorder")
            .createdDateTime(LocalDateTime.now())
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId("recorded")
            .build();

        statusHistory = StatusHistory.builder()
            .changeByName("John Doe")
            .changeByUserId("jp-recorder")
            .changeDateTime(LocalDateTime.now())
            .statusId(StatusId.RECORDED.name())
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
    void shouldReturnEmptyWhenHistoryNotFound() {
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

}

