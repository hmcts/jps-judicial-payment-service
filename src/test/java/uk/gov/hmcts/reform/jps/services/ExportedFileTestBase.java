package uk.gov.hmcts.reform.jps.services;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalTime;

class ExportedFileTestBase {

    @NotNull
    protected SittingRecord generateSittingRecord() {
        SittingRecord sittingRecord = new SittingRecord();
        sittingRecord.setAm(true);
        sittingRecord.setContractTypeId(1L);
        sittingRecord.setEpimmsId("42");
        sittingRecord.setHmctsServiceId("42");
        sittingRecord.setId(1L);
        sittingRecord.setJudgeRoleTypeId("42");
        sittingRecord.setPersonalCode("Personal Code");
        sittingRecord.setPm(true);
        sittingRecord.setRegionId("us-east-2");
        sittingRecord.setSittingDate(LocalDate.of(2023, 1, 1));
        sittingRecord.setStatusId(StatusId.RECORDED);
        StatusHistory statusHistory = generateStatusHistory();
        sittingRecord.addStatusHistory(statusHistory);
        return sittingRecord;
    }

    @NotNull
    protected StatusHistory generateStatusHistory() {
        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setChangedByName("Changed By Name");
        statusHistory.setChangedByUserId("42");
        statusHistory.setChangedDateTime(LocalDate.of(2023, 1, 1).atStartOfDay());
        statusHistory.setId(1L);
        statusHistory.setSittingRecord(new SittingRecord());
        statusHistory.setStatusId(StatusId.RECORDED);
        return statusHistory;
    }

    @NotNull
    protected ExportedFile generateExportedFile(ExportedFileData exportedFileData,
                                             ExportedFileDataHeader exportedFileDataHeader) {
        ExportedFile exportedFile = new ExportedFile();
        exportedFile.addExportedFileData(exportedFileData);
        exportedFile.setExportedDateTime(LocalDate.of(2023, 1, 1).atStartOfDay());
        exportedFile.setExportedFileDataHeader(exportedFileDataHeader);
        exportedFile.setFileName("foo.txt");
        exportedFile.setFileRecordCount(3);
        exportedFile.setId(1L);
        return exportedFile;
    }

    @NotNull
    protected ExportedFileDataHeader generateExportedFileDataHeader() {
        ExportedFileDataHeader exportedFileDataHeader = new ExportedFileDataHeader();
        exportedFileDataHeader.addExportedFile(new ExportedFile());
        exportedFileDataHeader.setExportedBy("Exported By");
        exportedFileDataHeader.setExportedDateTime(LocalDate.of(2023, 1, 1).atStartOfDay());
        exportedFileDataHeader.setGroupName("Group Name");
        exportedFileDataHeader.setHmctsServiceId("hmcts1");
        exportedFileDataHeader.setId(1L);
        exportedFileDataHeader.setStatus("Status");
        return exportedFileDataHeader;
    }

    @NotNull
    protected ExportedFileData generateExportedFileData(SittingRecord sittingRecord) {
        ExportedFileData exportedFileData = new ExportedFileData();
        exportedFileData.setCostCenter("Cost Center");
        exportedFileData.setEmployeeNumber("42");
        exportedFileData.setEmployeesValue("42");
        exportedFileData.setExportedFile(new ExportedFile());
        exportedFileData.setFixedOrTempIndicator("Fixed Or Temp Indicator");
        exportedFileData.setId(1L);
        exportedFileData.setPayElementId(1L);
        exportedFileData.setPayElementStartDate(LocalDate.of(2023, 1, 1));
        exportedFileData.setPostId(1L);
        exportedFileData.setRecordType("Record Type");
        exportedFileData.setSittingRecord(sittingRecord);
        exportedFileData.setTransactionDate(LocalDate.of(2023, 1, 1));
        exportedFileData.setTransactionId(1L);
        exportedFileData.setTransactionTime(LocalTime.MIDNIGHT);
        return exportedFileData;
    }

}
