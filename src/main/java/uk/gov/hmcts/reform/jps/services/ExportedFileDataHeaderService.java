package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataHeaderRepository;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFileDataHeaderService {

    private final ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    @Transactional
    public void insertRecord(ExportedFileDataHeader exportedFileDataHeader) {
        exportedFileDataHeaderRepository.save(exportedFileDataHeader);
    }

}
