package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class SittingRecordRepositoryTest {

    @Autowired
    private SittingRecordRepository recordRepository;

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    @BeforeEach
    void setUp() {
        judicialOfficeHolderRepository.deleteAll();
        recordRepository.deleteAll();
    }

    @Test
    void shouldSaveSittingRecord() {

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode("001")
            .build();
        judicialOfficeHolderRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = SittingRecord.builder()
            .am(true)
            .contractTypeId(2L)
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("jp-recorder")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(StatusId.RECORDED.name())
            .build();
        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);

        assertThat(persistedSittingRecord).isNotNull();
        assertThat(persistedSittingRecord.getId()).isNotNull();
        assertThat(persistedSittingRecord).isEqualTo(sittingRecord);
    }

    @Test
    void shouldUpdateSittingRecordWhenRecordIsPresent() {

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode("001")
            .build();
        judicialOfficeHolderRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = SittingRecord.builder()
            .contractTypeId(2L)
            .createdDateTime(LocalDateTime.now())
            .createdByUserId("555")
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .pm(true)
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(StatusId.RECORDED.name())
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

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode("001")
            .build();
        judicialOfficeHolderRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = SittingRecord.builder()
            .am(true)
            .contractTypeId(2L)
            .createdByUserId("jp-recorder")
            .createdDateTime(LocalDateTime.now())
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .pm(true)
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(StatusId.RECORDED.name())
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
