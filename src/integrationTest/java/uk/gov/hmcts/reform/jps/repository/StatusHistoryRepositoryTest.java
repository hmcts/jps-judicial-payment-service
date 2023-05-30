package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureWireMock(port = 0)
@DataJpaTest
class StatusHistoryRepositoryTest {

    @Autowired
    private StatusHistoryRepository historyRepository;
    @Autowired
    private SittingRecordRepository recordRepository;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    private SittingRecord persistedSittingRecord;

    private StatusHistory persistedStatusHistory;

    private StatusHistory statusHistory;

    @BeforeEach
    public void setUp() {
        final String hostUrl = "http://localhost:" + wiremockPort;

        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId("recorded")
            .regionId("1")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("jp-recorder")
            .build();
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        statusHistory = StatusHistory.builder()
            .statusId("recorded")
            .sittingRecordId(persistedSittingRecord)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .changeByName("John Smith")
            .build();
        persistedStatusHistory = historyRepository.save(statusHistory);
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

}

