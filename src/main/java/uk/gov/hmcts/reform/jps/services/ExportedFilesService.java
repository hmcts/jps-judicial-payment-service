package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.repository.ExportedFilesRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFilesService {

    private final ExportedFilesRepository exportedFilesRepository;

    public void insertRecord(ExportedFile exportedFile) {
        exportedFilesRepository.save(exportedFile);
    }

    protected ExportedFile createExportedFile(ExportedFileDataHeader exportedFileDataHeader, String fileName,
                                              Integer fileRecordCount) {
        ExportedFile exportedFile = ExportedFile.builder()
            .exportedFileDataHeader(exportedFileDataHeader)
            .exportedDateTime(LocalDateTime.now())
            .fileName(fileName)
            .fileRecordCount(fileRecordCount)
            .build();
        insertRecord(exportedFile);
        return exportedFile;
    }
}
