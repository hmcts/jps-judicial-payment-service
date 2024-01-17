package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataHeaderRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportedFileDataHeaderServiceTest extends ExportedFileTestBase {

    @Mock
    private ExportedFileDataHeaderRepository exportedFileDataHeaderRepository;

    @InjectMocks
    private ExportedFileDataHeaderService exportedFileDataHeaderService;

    @Test
    void testInsertRecord() {

        SittingRecord sittingRecord = generateSittingRecord();
        ExportedFileData exportedFileData = generateExportedFileData(sittingRecord);
        ExportedFileDataHeader exportedFileDataHeader = generateExportedFileDataHeader();
        ExportedFile exportedFile = generateExportedFile(exportedFileData, exportedFileDataHeader);

        when(exportedFileDataHeaderRepository.save(Mockito.<ExportedFileDataHeader>any()))
            .thenReturn(exportedFileDataHeader);

        exportedFileDataHeaderService.insertRecord(exportedFileDataHeader);

        verify(exportedFileDataHeaderRepository).save(Mockito.<ExportedFileDataHeader>any());
        assertEquals("2023-01-01", exportedFileDataHeader.getExportedDateTime().toLocalDate().toString());
        assertEquals("hmcts1", exportedFileDataHeader.getHmctsServiceId());
        assertEquals("Exported By", exportedFileDataHeader.getExportedBy());
        assertEquals("Group Name", exportedFileDataHeader.getGroupName());
        assertEquals("Status", exportedFileDataHeader.getStatus());
        assertEquals(1L, exportedFileDataHeader.getId().longValue());
    }

    @Test
    void testCreateExportedFileDataHeader() {

        ExportedFileDataHeader actualCreateExportedFileDataHeaderResult = exportedFileDataHeaderService
            .createExportedFileDataHeader("Group Name", "Exported By", "hmcts1");

        // Assert
        verify(exportedFileDataHeaderRepository).save(Mockito.<ExportedFileDataHeader>any());
        assertEquals("hmcts1", actualCreateExportedFileDataHeaderResult.getHmctsServiceId());
        assertEquals("Exported By", actualCreateExportedFileDataHeaderResult.getExportedBy());
        assertEquals("Group Name", actualCreateExportedFileDataHeaderResult.getGroupName());
        assertEquals("Publish", actualCreateExportedFileDataHeaderResult.getStatus());
        assertNull(actualCreateExportedFileDataHeaderResult.getId());
        assertTrue(actualCreateExportedFileDataHeaderResult.getExportedFiles().isEmpty());
    }

}
