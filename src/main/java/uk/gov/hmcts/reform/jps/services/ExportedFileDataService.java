package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataRepository;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFileDataService {

    private final ExportedFileDataRepository exportedFileDataRepository;

    @Transactional
    public void insertRecord(ExportedFileData exportedFileData) {
        exportedFileDataRepository.save(exportedFileData);
    }

}
