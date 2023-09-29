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
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@ActiveProfiles("itest")
public class ExportedFilesRepositoryTest {
    @Autowired
    private ExportedFilesRepository exportedFilesRepository;

    @Autowired
    private ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    @Test
    @Sql(scripts = RESET_DATABASE)
    void shouldSaveExportedFileDataHeader() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFileDataHeader persistedHeader = exportedFileDataHeaderRepository.save(exportedFileDataHeader);
        ExportedFile exportedFile = createExportedFile(persistedHeader);
        ExportedFile persistedFile = exportedFilesRepository.save(exportedFile);
        assertThat(persistedFile.getId()).isNotNull();
    }


    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<ExportedFile> optionalExportedFile = exportedFilesRepository.findById(100L);
        assertThat(optionalExportedFile).isEmpty();
    }

    @Test
    @Sql(scripts = RESET_DATABASE)
    void shouldDeleteSelectedRecord() {
        ExportedFileDataHeader exportedFileDataHeader = createExportedFileDataHeader();
        ExportedFileDataHeader persistedHeader = exportedFileDataHeaderRepository.save(exportedFileDataHeader);
        ExportedFile exportedFile = createExportedFile(persistedHeader);
        ExportedFile persistedFile = exportedFilesRepository.save(exportedFile);

        exportedFilesRepository.deleteById(persistedFile.getId());
        Optional<ExportedFile> optionalExportedFile = exportedFilesRepository
            .findById(persistedFile.getId());

        assertThat(optionalExportedFile).isEmpty();
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
}
