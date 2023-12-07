package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataRepository;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ExportedFileDataService {

    private final ExportedFileDataRepository exportedFileDataRepository;

    public void insertRecord(ExportedFileData exportedFileData) {
        exportedFileDataRepository.save(exportedFileData);
    }

    protected ExportedFileData createExportedFileData(SittingRecord sittingRecord, CourtVenue courtVenue,
                                                      JohPayroll johPayroll) {
        ExportedFileData exportedFileData = ExportedFileData.builder()
            .sittingRecord(sittingRecord)
            //.recordType(recordType)
            //.transactionId(transactionId)
            //.employeeNumber(employeeNumber)
            //.transactionDate(transactionDate)
            //.transactionTime(transactionTime)
            .payElementId(Long.valueOf(johPayroll.getPayrollId()))
            .payElementStartDate(sittingRecord.getSittingDate())
            //.fixedOrTempIndicator(fixedOrTempIndicator)
            //.employeesValue()
            //.postId(postId)
            .costCenter(courtVenue.getCostCenterCode())
            .build();
        insertRecord(exportedFileData);
        return exportedFileData;
    }
}
