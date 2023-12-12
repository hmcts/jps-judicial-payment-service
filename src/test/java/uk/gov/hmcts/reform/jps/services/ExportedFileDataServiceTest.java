package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;
import uk.gov.hmcts.reform.jps.domain.ExportedFile;
import uk.gov.hmcts.reform.jps.domain.ExportedFileData;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.ExportedFileDataRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExportedFileDataServiceTest extends ExportedFileTestBase {

    @Mock
    private ExportedFileDataRepository exportedFileDataRepository;

    @InjectMocks
    private ExportedFileDataService exportedFileDataService;

    @Test
    void testCreateExportedFileData() {

        SittingRecord sittingRecord = generateSittingRecord();
        ExportedFileData exportedFileData = generateExportedFileData(sittingRecord);
        ExportedFileDataHeader exportedFileDataHeader = generateExportedFileDataHeader();
        ExportedFile exportedFile = generateExportedFile(exportedFileData, exportedFileDataHeader);

        CourtVenue courtVenue = generateCourtVenue();

        JohAttributes johAttributes = generateJohAttributes();
        JohPayroll johPayroll = generateJohPayroll();
        JudicialOfficeHolder judicialOfficeHolder = generateJudicialOfficeHolder(johAttributes, johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        johAttributes.setJudicialOfficeHolder(judicialOfficeHolder);

        ExportedFileData actualCreateExportedFileDataResult =
            exportedFileDataService.createExportedFileData(sittingRecord, courtVenue, johPayroll);

        verify(exportedFileDataRepository).save(Mockito.<ExportedFileData>any());
        assertEquals("2023-01-01", actualCreateExportedFileDataResult.getPayElementStartDate().toString());
        assertEquals("costCenterCode", actualCreateExportedFileDataResult.getCostCenter());
        assertNull(actualCreateExportedFileDataResult.getId());
        assertNull(actualCreateExportedFileDataResult.getPostId());
        assertNull(actualCreateExportedFileDataResult.getTransactionId());
        assertNull(actualCreateExportedFileDataResult.getEmployeeNumber());
        assertNull(actualCreateExportedFileDataResult.getEmployeesValue());
        assertNull(actualCreateExportedFileDataResult.getFixedOrTempIndicator());
        assertNull(actualCreateExportedFileDataResult.getRecordType());
        assertNull(actualCreateExportedFileDataResult.getTransactionDate());
        assertNull(actualCreateExportedFileDataResult.getTransactionTime());
        assertNull(actualCreateExportedFileDataResult.getExportedFile());
        assertEquals(1L, actualCreateExportedFileDataResult.getPayElementId().longValue());
        assertSame(sittingRecord, actualCreateExportedFileDataResult.getSittingRecord());
    }

    private static JohPayroll generateJohPayroll() {
        JohPayroll johPayroll = new JohPayroll();
        johPayroll.setEffectiveStartDate(LocalDate.of(2023, 1, 1));
        johPayroll.setId(1L);
        johPayroll.setJudgeRoleTypeId("judge");
        johPayroll.setJudicialOfficeHolder(new JudicialOfficeHolder());
        johPayroll.setPayrollId(Long.toString(1L));
        return johPayroll;
    }

    private static JohAttributes generateJohAttributes() {
        JohAttributes johAttributes = new JohAttributes();
        johAttributes.setCrownServantFlag(true);
        johAttributes.setEffectiveStartDate(LocalDate.of(2023, 1, 1));
        johAttributes.setId(1L);
        johAttributes.setJudicialOfficeHolder(new JudicialOfficeHolder());
        johAttributes.setLondonFlag(true);
        return johAttributes;
    }

    private static JudicialOfficeHolder generateJudicialOfficeHolder(JohAttributes johAttributes,
                                                                     JohPayroll johPayroll) {
        JudicialOfficeHolder judicialOfficeHolder = new JudicialOfficeHolder();
        judicialOfficeHolder.addJohAttributes(johAttributes);
        judicialOfficeHolder.addJohPayroll(johPayroll);
        judicialOfficeHolder.setId(1L);
        judicialOfficeHolder.setPersonalCode("personalCode");
        return judicialOfficeHolder;
    }

    private static CourtVenue generateCourtVenue() {
        CourtVenue courtVenue = new CourtVenue();
        courtVenue.setCostCenterCode("costCenterCode");
        courtVenue.setEpimmsId("epId1");
        courtVenue.setHmctsServiceId("hmcts1");
        courtVenue.setId(1L);
        return courtVenue;
    }
}
