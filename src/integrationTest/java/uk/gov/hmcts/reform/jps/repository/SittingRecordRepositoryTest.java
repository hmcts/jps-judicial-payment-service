package uk.gov.hmcts.reform.jps.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Slf4j
class SittingRecordRepositoryTest {

    @Autowired
    private SittingRecordRepository recordRepository;

    @Test
    void shouldSaveSittingRecord() {
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
        log.debug("Has record persisted ", persistedSittingRecord);
        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertThat(persistedSittingRecord).isEqualTo(sittingRecord);
    }

    @Test
    void shouldUpdateSittingRecordWhenRecordIsPresent() {
        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId("recorded")
            .regionId("1")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .pm(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("555")
            .build();
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> optionalSettingRecordToUpdate = recordRepository
            .findById(persistedSittingRecord.getId());
        assertThat(optionalSettingRecordToUpdate).isPresent();

        SittingRecord settingRecordToUpdate = optionalSettingRecordToUpdate.get();
        settingRecordToUpdate.setSittingDate(LocalDate.now().minusDays(30));
        settingRecordToUpdate.setChangeDateTime(LocalDateTime.now());
        settingRecordToUpdate.setChangeByUserId("jp-submitter");

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
        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId("recorded")
            .regionId("1")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .personalCode("001")
            .contractTypeId(2L)
            .am(true)
            .pm(true)
            .judgeRoleTypeId("HighCourt")
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("jp-recorder")
            .build();

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        Optional<SittingRecord> optionalSettingRecordToUpdate = recordRepository
            .findById(persistedSittingRecord.getId());
        assertThat(optionalSettingRecordToUpdate).isPresent();

        SittingRecord settingRecordToDelete = optionalSettingRecordToUpdate.get();
        recordRepository.deleteById(settingRecordToDelete.getId());

        optionalSettingRecordToUpdate = recordRepository.findById(settingRecordToDelete.getId());
        assertThat(optionalSettingRecordToUpdate).isEmpty();
    }
}
