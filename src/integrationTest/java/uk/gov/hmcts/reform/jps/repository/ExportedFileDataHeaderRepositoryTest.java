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
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class ExportedFileDataHeaderRepositoryTest {
    public static final LocalTime NOW = LocalTime.now();
    @Autowired
    private ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    private static final LocalDate LOCAL_DATE = LocalDate.now();
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();

    @Test
    @Sql(scripts = RESET_DATABASE)
    void shouldSaveExportedFileDataHeader() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFileDataHeader persisted = exportedFileDataHeaderRepository.save(exportedFileDataHeader);
        assertThat(persisted.getId()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<ExportedFileDataHeader> optionalExportedFileDataHeader = exportedFileDataHeaderRepository
            .findById(100L);
        assertThat(optionalExportedFileDataHeader).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        exportedFileDataHeaderRepository.save(exportedFileDataHeader);
        exportedFileDataHeaderRepository.deleteById(exportedFileDataHeader.getId());

        Optional<ExportedFileDataHeader> optionalExportedFileDataHeader = exportedFileDataHeaderRepository
            .findById(exportedFileDataHeader.getId());

        assertThat(optionalExportedFileDataHeader).isEmpty();
    }

    @Test
    @Sql(RESET_DATABASE)
    void shouldAddExportedFile() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFile exportedFile = createExportedFile();
        exportedFileDataHeader.addExportedFile(exportedFile);

        ExportedFileDataHeader persistedExportedFileDataHeader = exportedFileDataHeaderRepository
            .save(exportedFileDataHeader);

        Optional<ExportedFileDataHeader> fetchedExportedFileDataHeader = exportedFileDataHeaderRepository.findById(
            persistedExportedFileDataHeader.getId());

        assertThat(fetchedExportedFileDataHeader.stream())
            .flatMap(ExportedFileDataHeader::getExportedFiles)
            .map(
                ExportedFile::getId,
                ExportedFile::getFileName,
                ExportedFile::getFileRecordCount
            )
            .contains(tuple(
                1L,
                "payments",
                10)
            );
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldAddExportedFileData() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFile exportedFile = createExportedFile();
        exportedFileDataHeader.addExportedFile(exportedFile);

        ExportedFileData exportedFIleData = createExportedFileData();
        exportedFile.addExportedFileData(exportedFIleData);

        SittingRecord sittingRecord = createSittingRecord();
        exportedFIleData.setSittingRecord(sittingRecord);


        ExportedFileDataHeader persistedExportedFileDataHeader = exportedFileDataHeaderRepository
            .save(exportedFileDataHeader);

        Optional<ExportedFileDataHeader> fetchedExportedFileDataHeader = exportedFileDataHeaderRepository.findById(
            persistedExportedFileDataHeader.getId());

        assertThat(fetchedExportedFileDataHeader.stream())
            .flatMap(ExportedFileDataHeader::getExportedFiles)
            .map(
                ExportedFile::getId,
                ExportedFile::getFileName,
                ExportedFile::getFileRecordCount
            )
            .contains(tuple(
                1L,
                "payments",
                10)
            );

        assertThat(fetchedExportedFileDataHeader.stream())
            .flatMap(ExportedFileDataHeader::getExportedFiles)
            .flatMap(ExportedFile::getExportedFileRecords)
            .map(
                ExportedFileData::getId,
                ExportedFileData::getRecordType,
                ExportedFileData::getTransactionId,
                ExportedFileData::getEmployeeNumber,
                ExportedFileData::getTransactionDate,
                ExportedFileData::getTransactionTime,
                ExportedFileData::getPayElementId,
                ExportedFileData::getPayElementStartDate,
                ExportedFileData::getFixedOrTempIndicator,
                ExportedFileData::getEmployeesValue,
                ExportedFileData::getPostId,
                ExportedFileData::getCostCenter
            )
            .contains(tuple(
                1L,
                "test",
                1L,
                "test-123",
                LOCAL_DATE,
                NOW,
                2L,
                LOCAL_DATE,
                "fixed",
                "high",
                3L,
                "London"));
    }

    private ExportedFileDataHeader createExportedFileDataHeader() {
        return ExportedFileDataHeader.builder()
            .exportedDateTime(LOCAL_DATE_TIME)
            .groupName("Test")
            .exportedBy("Publisher")
            .status("exported")
            .hmctsServiceId("BBA3")
            .build();
    }

    private ExportedFile createExportedFile() {
        return ExportedFile.builder()
            .fileName("payments")
            .fileRecordCount(10)
            .build();
    }

    private ExportedFileData createExportedFileData() {
        return ExportedFileData.builder()
            .recordType("test")
            .transactionId(1L)
            .employeeNumber("test-123")
            .transactionDate(LOCAL_DATE)
            .transactionTime(NOW)
            .payElementId(2L)
            .payElementStartDate(LOCAL_DATE)
            .fixedOrTempIndicator("fixed")
            .employeesValue("high")
            .postId(3L)
            .costCenter("London")
            .build();
    }

    private SittingRecord createSittingRecord() {
        return SittingRecord.builder()
            .id(1L)
            .build();
    }
}
