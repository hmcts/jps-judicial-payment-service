package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class ExportedFileDataHeaderRepositoryTest {
    @Autowired
    private ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

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
            .contains(tuple(1L, "payments", 10));
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

    private ExportedFile createExportedFile() {
        return ExportedFile.builder()
            .exportedDateTime(LocalDateTime.now())
            .fileName("payments")
            .fileRecordCount(10)
            .build();
    }

}
