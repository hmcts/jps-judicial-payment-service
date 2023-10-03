package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class ExportedFileDataRepositoryTest {
    @Autowired
    private ExportedFilesRepository exportedFilesRepository;

    @Autowired
    private ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    @Autowired
    private SittingRecordRepository sittingRecordRepository;

    @Autowired
    private ExportedFileDataRepository exportedFileDataRepository;

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldSaveExportedFileData() {
        getRecordPersisted();
    }

    private ExportedFileData getRecordPersisted() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFileDataHeader persistedHeader = exportedFileDataHeaderRepository.save(exportedFileDataHeader);
        ExportedFile exportedFile = createExportedFile(persistedHeader);
        ExportedFile persistedFile = exportedFilesRepository.save(exportedFile);
        Optional<SittingRecord> sittingRecord = sittingRecordRepository.findById(1L);
        assertThat(sittingRecord)
            .isPresent();
        ExportedFileData exportedFileData = createExportedFileData(persistedFile, sittingRecord.get());
        ExportedFileData persistedExportedFileData = exportedFileDataRepository.save(exportedFileData);
        assertThat(persistedExportedFileData.getId()).isNotNull();

        return persistedExportedFileData;
    }


    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<ExportedFileData> optionalExportedFileData = exportedFileDataRepository.findById(100L);
        assertThat(optionalExportedFileData).isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldDeleteSelectedRecord() {
        ExportedFileData persistedExportedFileData = getRecordPersisted();

        exportedFileDataRepository.deleteById(persistedExportedFileData.getId());
        Optional<ExportedFileData> optionalExportedFileData = exportedFileDataRepository
            .findById(persistedExportedFileData.getId());

        assertThat(optionalExportedFileData).isEmpty();
    }

    private ExportedFileDataHeader createExportedFileDataHeader() {
        return ExportedFileDataHeader.builder()
            .exportedDateTime(LocalDateTime.now())
            .groupName("Test")
            .exportedBy("Publisher")
            .status("exported")
            .hmctsServiceId("BBA3")
            .build();
    }

    private ExportedFile createExportedFile(ExportedFileDataHeader exportedFileDataHeader) {
        return ExportedFile.builder()
            .exportedFileDataHeader(exportedFileDataHeader)
            .fileName("payments")
            .fileRecordCount(10)
            .build();
    }

    private ExportedFileData createExportedFileData(ExportedFile exportedFile, SittingRecord sittingRecord) {
        return ExportedFileData.builder()
            .exportedFile(exportedFile)
            .sittingRecord(sittingRecord)
            .recordType("test")
            .transactionId(1L)
            .employeeNumber("test-123")
            .transactionDate(LocalDate.now())
            .transactionTime(LocalTime.now())
            .payElementId(2L)
            .payElementStartDate(LocalDate.now())
            .fixedOrTempIndicator("fixed")
            .employeesValue("high")
            .postId(3L)
            .costCenter("London")
            .build();
    }
}
