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
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        SittingRecord sittingRecord = createSittingRecord(true, 1L, "EP1", "HM1",
                                                          1L, "JRT1", "PC1", true,
                                                          "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                          StatusId.RECORDED.name());
        StatusHistory statusHistory = createStatusHistory("Jason Bourne", "11233",
                                                          LocalDateTime.now(),  1L, StatusId.RECORDED.name());
        statusHistory.setSittingRecord(sittingRecord);
        sittingRecord.addStatusHistory(statusHistory);

        SittingRecord sittingRecord2 = createSittingRecord(true, 1L, "EP2", "HM2",
                                                           2L, "42", "PC1", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory2 = createStatusHistory("Matt Murdock", "11244",
                                                          LocalDateTime.now(),  2L, StatusId.RECORDED.name());
        statusHistory2.setSittingRecord(sittingRecord2);
        sittingRecord2.addStatusHistory(statusHistory2);

        SittingRecord sittingRecord3 = createSittingRecord(true, 1L, "EP3", "HM3",
                                                           3L, "42", "PC1", true,
                                                           "us-east-2",
                                                            LocalDate.of(1970, 1, 1),
                                                           "42");
        StatusHistory statusHistory3 = createStatusHistory("Stephen Strange", "11255",
                                                           LocalDateTime.now(),  3L, StatusId.RECORDED.name());
        statusHistory3.setSittingRecord(sittingRecord3);
        sittingRecord3.addStatusHistory(statusHistory3);

        when(sittingRecordRepository.save(Mockito.any())).thenReturn(sittingRecord3);

        SittingRecord sittingRecord4 = createSittingRecord(true, 1L, "42", "42",
                                                           4L, "42", "PC1", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory4 = createStatusHistory("Bruce Wayne", "11266",
                                                          LocalDateTime.now(),  4L, StatusId.RECORDED.name());
        statusHistory4.setSittingRecord(sittingRecord4);
        sittingRecord4.addStatusHistory(statusHistory4);

        SittingRecord sittingRecord5 = createSittingRecord(true, 1L, "EP5", "HM5",
                                                           5L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory5 = createStatusHistory("Lois Lane", "11277",
                                                         LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           5L, StatusId.RECORDED.name());
        statusHistory5.setSittingRecord(sittingRecord5);
        sittingRecord5.addStatusHistory(statusHistory5);
        when(statusHistoryRepository.save(Mockito.any())).thenReturn(statusHistory5);

        SittingRecord sittingRecord6 = createSittingRecord(true, 1L, "42", "42",
                                                           6L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory6 = createStatusHistory("Lois Lane", "11277",
                                                           LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           6L, StatusId.RECORDED.name());
        statusHistory4.setSittingRecord(sittingRecord6);
        sittingRecord4.addStatusHistory(statusHistory6);

        SittingRecord sittingRecord7 = createSittingRecord(true, 1L, "42", "42",
                                                           7L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory7 = createStatusHistory("Lois Lane", "11277",
                                                           LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           7L, StatusId.RECORDED.name());
        statusHistory7.setSittingRecord(sittingRecord7);
        sittingRecord7.addStatusHistory(statusHistory7);

        SittingRecord sittingRecord8 = createSittingRecord(true, 1L, "42", "42",
                                                           8L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory8 = createStatusHistory("Lois Lane", "11277",
                                                           LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           8L, StatusId.RECORDED.name());
        statusHistory8.setSittingRecord(sittingRecord8);
        sittingRecord8.addStatusHistory(statusHistory8);

        SittingRecord sittingRecord9 = createSittingRecord(true, 1L, "42", "42",
                                                           9L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory9 = createStatusHistory("Lois Lane", "11277",
                                                           LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           9L, StatusId.RECORDED.name());
        statusHistory9.setSittingRecord(sittingRecord9);
        sittingRecord9.addStatusHistory(statusHistory9);

        statusHistoryService.saveStatusHistory(statusHistory9, sittingRecord9);
        verify(sittingRecordRepository).save(Mockito.any());
        verify(statusHistoryRepository).save(Mockito.any());
        SittingRecord sittingRecordRetrieved = statusHistory9.getSittingRecord();
        assertSame(sittingRecord9, sittingRecordRetrieved);
        assertEquals(2, sittingRecordRetrieved.getStatusHistories().size());
        assertEquals(StatusId.RECORDED.name(), sittingRecordRetrieved.getStatusId());
    }

    private StatusHistory createStatusHistory(String changeByName, String changeByUserId, LocalDateTime changeDateTime,
                                              Long id, String statusId) {

        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setChangeByName(changeByName);
        statusHistory.setChangeByUserId(changeByUserId);
        statusHistory.setChangeDateTime(changeDateTime);
        statusHistory.setId(id);
        statusHistory.setStatusId(statusId);
        return statusHistory;
    }


    private SittingRecord createSittingRecord(Boolean am, Long contractTypeId, String epimsId, String hmctsServiceId,
                                               Long id, String judgeRoleTypeId, String personalCode, Boolean pm,
                                               String regionId, LocalDate sittingDate, String statusId) {
        SittingRecord sittingRecord = new SittingRecord();
        sittingRecord.setAm(am);
        sittingRecord.setContractTypeId(contractTypeId);
        sittingRecord.setEpimsId(epimsId);
        sittingRecord.setHmctsServiceId(hmctsServiceId);
        sittingRecord.setId(id);
        sittingRecord.setJudgeRoleTypeId(judgeRoleTypeId);
        sittingRecord.setPersonalCode(personalCode);
        sittingRecord.setPm(pm);
        sittingRecord.setRegionId(regionId);
        sittingRecord.setSittingDate(sittingDate);
        sittingRecord.setStatusId(statusId);

        return sittingRecord;
    }
}

