package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StatusHistoryService.class})
@ExtendWith(SpringExtension.class)
class StatusHistoryServiceTest {
    @MockBean
    private SittingRecordRepository sittingRecordRepository;

    @MockBean
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private StatusHistoryService statusHistoryService;

    /**
     * Method under test: {@link StatusHistoryService#saveStatusHistory(StatusHistory, SittingRecord)}.
     */
    @Test
    void shouldSaveStatusHistory() {
        SittingRecord sittingRecord = new SittingRecord();
        sittingRecord.addStatusHistory(new StatusHistory());
        sittingRecord.setAm(true);
        sittingRecord.setContractTypeId(1L);
        sittingRecord.setEpimsId("42");
        sittingRecord.setHmctsServiceId("42");
        sittingRecord.setId(1L);
        sittingRecord.setJudgeRoleTypeId("42");
        sittingRecord.setPersonalCode("Personal Code");
        sittingRecord.setPm(true);
        sittingRecord.setRegionId("us-east-2");
        sittingRecord.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord.setStatusId("42");

        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setChangeByName("Change By Name");
        statusHistory.setChangeByUserId("42");
        statusHistory.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory.setId(1L);
        statusHistory.setSittingRecord(sittingRecord);
        statusHistory.setStatusId("42");

        SittingRecord sittingRecord2 = new SittingRecord();
        sittingRecord2.addStatusHistory(statusHistory);
        sittingRecord2.setAm(true);
        sittingRecord2.setContractTypeId(1L);
        sittingRecord2.setEpimsId("42");
        sittingRecord2.setHmctsServiceId("42");
        sittingRecord2.setId(1L);
        sittingRecord2.setJudgeRoleTypeId("42");
        sittingRecord2.setPersonalCode("Personal Code");
        sittingRecord2.setPm(true);
        sittingRecord2.setRegionId("us-east-2");
        sittingRecord2.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord2.setStatusId("42");

        StatusHistory statusHistory2 = new StatusHistory();
        statusHistory2.setChangeByName("Change By Name");
        statusHistory2.setChangeByUserId("42");
        statusHistory2.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory2.setId(1L);
        statusHistory2.setSittingRecord(sittingRecord2);
        statusHistory2.setStatusId("42");

        SittingRecord sittingRecord3 = new SittingRecord();
        sittingRecord3.addStatusHistory(statusHistory2);
        sittingRecord3.setAm(true);
        sittingRecord3.setContractTypeId(1L);
        sittingRecord3.setEpimsId("42");
        sittingRecord3.setHmctsServiceId("42");
        sittingRecord3.setId(1L);
        sittingRecord3.setJudgeRoleTypeId("42");
        sittingRecord3.setPersonalCode("Personal Code");
        sittingRecord3.setPm(true);
        sittingRecord3.setRegionId("us-east-2");
        sittingRecord3.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord3.setStatusId("42");
        when(sittingRecordRepository.save(Mockito.any())).thenReturn(sittingRecord3);

        StatusHistory statusHistory3 = new StatusHistory();
        statusHistory3.setChangeByName("Change By Name");
        statusHistory3.setChangeByUserId("42");
        statusHistory3.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory3.setId(1L);
        statusHistory3.setSittingRecord(new SittingRecord());
        statusHistory3.setStatusId("42");

        SittingRecord sittingRecord4 = new SittingRecord();
        sittingRecord4.addStatusHistory(statusHistory3);
        sittingRecord4.setAm(true);
        sittingRecord4.setContractTypeId(1L);
        sittingRecord4.setEpimsId("42");
        sittingRecord4.setHmctsServiceId("42");
        sittingRecord4.setId(1L);
        sittingRecord4.setJudgeRoleTypeId("42");
        sittingRecord4.setPersonalCode("Personal Code");
        sittingRecord4.setPm(true);
        sittingRecord4.setRegionId("us-east-2");
        sittingRecord4.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord4.setStatusId("42");

        StatusHistory statusHistory4 = new StatusHistory();
        statusHistory4.setChangeByName("Change By Name");
        statusHistory4.setChangeByUserId("42");
        statusHistory4.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory4.setId(1L);
        statusHistory4.setSittingRecord(sittingRecord4);
        statusHistory4.setStatusId("42");

        SittingRecord sittingRecord5 = new SittingRecord();
        sittingRecord5.addStatusHistory(statusHistory4);
        sittingRecord5.setAm(true);
        sittingRecord5.setContractTypeId(1L);
        sittingRecord5.setEpimsId("42");
        sittingRecord5.setHmctsServiceId("42");
        sittingRecord5.setId(1L);
        sittingRecord5.setJudgeRoleTypeId("42");
        sittingRecord5.setPersonalCode("Personal Code");
        sittingRecord5.setPm(true);
        sittingRecord5.setRegionId("us-east-2");
        sittingRecord5.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord5.setStatusId("42");

        StatusHistory statusHistory5 = new StatusHistory();
        statusHistory5.setChangeByName("Change By Name");
        statusHistory5.setChangeByUserId("42");
        statusHistory5.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory5.setId(1L);
        statusHistory5.setSittingRecord(sittingRecord5);
        statusHistory5.setStatusId("42");
        when(statusHistoryRepository.save(Mockito.any())).thenReturn(statusHistory5);

        SittingRecord sittingRecord6 = new SittingRecord();
        sittingRecord6.addStatusHistory(new StatusHistory());
        sittingRecord6.setAm(true);
        sittingRecord6.setContractTypeId(1L);
        sittingRecord6.setEpimsId("42");
        sittingRecord6.setHmctsServiceId("42");
        sittingRecord6.setId(1L);
        sittingRecord6.setJudgeRoleTypeId("42");
        sittingRecord6.setPersonalCode("Personal Code");
        sittingRecord6.setPm(true);
        sittingRecord6.setRegionId("us-east-2");
        sittingRecord6.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord6.setStatusId("42");

        StatusHistory statusHistory6 = new StatusHistory();
        statusHistory6.setChangeByName("Change By Name");
        statusHistory6.setChangeByUserId("42");
        statusHistory6.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory6.setId(1L);
        statusHistory6.setSittingRecord(sittingRecord6);
        statusHistory6.setStatusId("42");

        SittingRecord sittingRecord7 = new SittingRecord();
        sittingRecord7.addStatusHistory(statusHistory6);
        sittingRecord7.setAm(true);
        sittingRecord7.setContractTypeId(1L);
        sittingRecord7.setEpimsId("42");
        sittingRecord7.setHmctsServiceId("42");
        sittingRecord7.setId(1L);
        sittingRecord7.setJudgeRoleTypeId("42");
        sittingRecord7.setPersonalCode("Personal Code");
        sittingRecord7.setPm(true);
        sittingRecord7.setRegionId("us-east-2");
        sittingRecord7.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord7.setStatusId("42");

        StatusHistory statusHistory7 = new StatusHistory();
        statusHistory7.setChangeByName("Change By Name");
        statusHistory7.setChangeByUserId("42");
        statusHistory7.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory7.setId(1L);
        statusHistory7.setSittingRecord(sittingRecord7);
        statusHistory7.setStatusId("42");

        StatusHistory statusHistory8 = new StatusHistory();
        statusHistory8.setChangeByName("Change By Name");
        statusHistory8.setChangeByUserId("42");
        statusHistory8.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory8.setId(1L);
        statusHistory8.setSittingRecord(new SittingRecord());
        statusHistory8.setStatusId("42");

        SittingRecord sittingRecord8 = new SittingRecord();
        sittingRecord8.addStatusHistory(statusHistory8);
        sittingRecord8.setAm(true);
        sittingRecord8.setContractTypeId(1L);
        sittingRecord8.setEpimsId("42");
        sittingRecord8.setHmctsServiceId("42");
        sittingRecord8.setId(1L);
        sittingRecord8.setJudgeRoleTypeId("42");
        sittingRecord8.setPersonalCode("Personal Code");
        sittingRecord8.setPm(true);
        sittingRecord8.setRegionId("us-east-2");
        sittingRecord8.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord8.setStatusId("42");

        StatusHistory statusHistory9 = new StatusHistory();
        statusHistory9.setChangeByName("Change By Name");
        statusHistory9.setChangeByUserId("42");
        statusHistory9.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory9.setId(1L);
        statusHistory9.setSittingRecord(sittingRecord8);
        statusHistory9.setStatusId("42");

        SittingRecord sittingRecord9 = new SittingRecord();
        sittingRecord9.addStatusHistory(statusHistory9);
        sittingRecord9.setAm(true);
        sittingRecord9.setContractTypeId(1L);
        sittingRecord9.setEpimsId("42");
        sittingRecord9.setHmctsServiceId("42");
        sittingRecord9.setId(1L);
        sittingRecord9.setJudgeRoleTypeId("42");
        sittingRecord9.setPersonalCode("Personal Code");
        sittingRecord9.setPm(true);
        sittingRecord9.setRegionId("us-east-2");
        sittingRecord9.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord9.setStatusId("42");
        statusHistoryService.saveStatusHistory(statusHistory7, sittingRecord9);
        verify(sittingRecordRepository).save(Mockito.any());
        verify(statusHistoryRepository).save(Mockito.any());
        SittingRecord sittingRecord10 = statusHistory7.getSittingRecord();
        assertSame(sittingRecord9, sittingRecord10);
        assertEquals(2, sittingRecord10.getStatusHistories().size());
        assertEquals("42", sittingRecord10.getStatusId());
    }
}

