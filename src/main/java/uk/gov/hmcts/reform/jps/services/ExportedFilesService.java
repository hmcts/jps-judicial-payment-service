package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.repository.ExportedFilesRepository;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFilesService {

    private final ExportedFilesRepository exportedFilesRepository;

    @Transactional
    public void insertRecord(ExportedFile exportedFile) {
        exportedFilesRepository.save(exportedFile);
    }
}
