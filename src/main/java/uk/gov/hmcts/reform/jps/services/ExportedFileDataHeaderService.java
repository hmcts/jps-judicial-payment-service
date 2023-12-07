package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataHeaderRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFileDataHeaderService {

    private final ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    public void insertRecord(ExportedFileDataHeader exportedFileDataHeader) {
        exportedFileDataHeaderRepository.save(exportedFileDataHeader);
    }

    public ExportedFileDataHeader createExportedFileDataHeader(String groupName, String exportedBy,
                                                                  String hmctsServiceId) {
        ExportedFileDataHeader exportedFileDataHeader =  ExportedFileDataHeader.builder()
            .exportedDateTime(LocalDateTime.now())
            .groupName(groupName)
            .exportedBy(exportedBy)
            .status("Publish")
            .hmctsServiceId(hmctsServiceId)
            .build();
        insertRecord(exportedFileDataHeader);
        return exportedFileDataHeader;
    }
}
