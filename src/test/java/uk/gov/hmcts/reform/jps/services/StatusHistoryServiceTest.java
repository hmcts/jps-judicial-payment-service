package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.components.BaseEvaluateDuplicate;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ContextConfiguration(classes = {StatusHistoryService.class})
@ExtendWith(SpringExtension.class)
class StatusHistoryServiceTest extends BaseEvaluateDuplicate {
    @MockBean
    private SittingRecordRepository sittingRecordRepository;

    @MockBean
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private StatusHistoryService statusHistoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }


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
        statusHistory.setStatusId(RECORDED);

        SittingRecord sittingRecord2 = new SittingRecord();
        sittingRecord2.addStatusHistory(statusHistory);
        sittingRecord2.setAm(true);
        sittingRecord2.setContractTypeId(1L);
        sittingRecord2.setEpimmsId("42");
        sittingRecord2.setHmctsServiceId("42");
        sittingRecord2.setId(1L);
        sittingRecord2.setJudgeRoleTypeId("42");
        sittingRecord2.setPersonalCode("Personal Code");
        sittingRecord2.setPm(true);
        sittingRecord2.setRegionId("us-east-2");
        sittingRecord2.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord2.setStatusId(RECORDED);

        StatusHistory statusHistory2 = new StatusHistory();
        statusHistory2.setChangeByName("Change By Name");
        statusHistory2.setChangeByUserId("42");
        statusHistory2.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory2.setId(1L);
        statusHistory2.setSittingRecord(sittingRecord2);
        statusHistory2.setStatusId(RECORDED);

        SittingRecord sittingRecord3 = new SittingRecord();
        sittingRecord3.addStatusHistory(statusHistory2);
        sittingRecord3.setAm(true);
        sittingRecord3.setContractTypeId(1L);
        sittingRecord3.setEpimmsId("42");
        sittingRecord3.setHmctsServiceId("42");
        sittingRecord3.setId(1L);
        sittingRecord3.setJudgeRoleTypeId("42");
        sittingRecord3.setPersonalCode("Personal Code");
        sittingRecord3.setPm(true);
        sittingRecord3.setRegionId("us-east-2");
        sittingRecord3.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord3.setStatusId(RECORDED);
        when(sittingRecordRepository.save(Mockito.any())).thenReturn(sittingRecord3);

        StatusHistory statusHistory3 = new StatusHistory();
        statusHistory3.setChangeByName("Change By Name");
        statusHistory3.setChangeByUserId("42");
        statusHistory3.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory3.setId(1L);
        statusHistory3.setSittingRecord(new SittingRecord());
        statusHistory3.setStatusId(RECORDED);

        SittingRecord sittingRecord4 = new SittingRecord();
        sittingRecord4.addStatusHistory(statusHistory3);
        sittingRecord4.setAm(true);
        sittingRecord4.setContractTypeId(1L);
        sittingRecord4.setEpimmsId("42");
        sittingRecord4.setHmctsServiceId("42");
        sittingRecord4.setId(1L);
        sittingRecord4.setJudgeRoleTypeId("42");
        sittingRecord4.setPersonalCode("Personal Code");
        sittingRecord4.setPm(true);
        sittingRecord4.setRegionId("us-east-2");
        sittingRecord4.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord4.setStatusId(RECORDED);

        StatusHistory statusHistory4 = new StatusHistory();
        statusHistory4.setChangeByName("Change By Name");
        statusHistory4.setChangeByUserId("42");
        statusHistory4.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory4.setId(1L);
        statusHistory4.setSittingRecord(sittingRecord4);
        statusHistory4.setStatusId(RECORDED);

        SittingRecord sittingRecord5 = new SittingRecord();
        sittingRecord5.addStatusHistory(statusHistory4);
        sittingRecord5.setAm(true);
        sittingRecord5.setContractTypeId(1L);
        sittingRecord5.setEpimmsId("42");
        sittingRecord5.setHmctsServiceId("42");
        sittingRecord5.setId(1L);
        sittingRecord5.setJudgeRoleTypeId("42");
        sittingRecord5.setPersonalCode("Personal Code");
        sittingRecord5.setPm(true);
        sittingRecord5.setRegionId("us-east-2");
        sittingRecord5.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord5.setStatusId(RECORDED);

        StatusHistory statusHistory5 = new StatusHistory();
        statusHistory5.setChangeByName("Change By Name");
        statusHistory5.setChangeByUserId("42");
        statusHistory5.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory5.setId(1L);
        statusHistory5.setSittingRecord(sittingRecord5);
        statusHistory5.setStatusId(RECORDED);
        when(statusHistoryRepository.save(Mockito.any())).thenReturn(statusHistory5);

        SittingRecord sittingRecord6 = new SittingRecord();
        sittingRecord6.addStatusHistory(new StatusHistory());
        sittingRecord6.setAm(true);
        sittingRecord6.setContractTypeId(1L);
        sittingRecord6.setEpimmsId("42");
        sittingRecord6.setHmctsServiceId("42");
        sittingRecord6.setId(1L);
        sittingRecord6.setJudgeRoleTypeId("42");
        sittingRecord6.setPersonalCode("Personal Code");
        sittingRecord6.setPm(true);
        sittingRecord6.setRegionId("us-east-2");
        sittingRecord6.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord6.setStatusId(RECORDED);

        StatusHistory statusHistory6 = new StatusHistory();
        statusHistory6.setChangeByName("Change By Name");
        statusHistory6.setChangeByUserId("42");
        statusHistory6.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory6.setId(1L);
        statusHistory6.setSittingRecord(sittingRecord6);
        statusHistory6.setStatusId(RECORDED);

        SittingRecord sittingRecord7 = new SittingRecord();
        sittingRecord7.addStatusHistory(statusHistory6);
        sittingRecord7.setAm(true);
        sittingRecord7.setContractTypeId(1L);
        sittingRecord7.setEpimmsId("42");
        sittingRecord7.setHmctsServiceId("42");
        sittingRecord7.setId(1L);
        sittingRecord7.setJudgeRoleTypeId("42");
        sittingRecord7.setPersonalCode("Personal Code");
        sittingRecord7.setPm(true);
        sittingRecord7.setRegionId("us-east-2");
        sittingRecord7.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord7.setStatusId(RECORDED);

        StatusHistory statusHistory7 = new StatusHistory();
        statusHistory7.setChangeByName("Change By Name");
        statusHistory7.setChangeByUserId("42");
        statusHistory7.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory7.setId(1L);
        statusHistory7.setSittingRecord(sittingRecord7);
        statusHistory7.setStatusId(RECORDED);

        StatusHistory statusHistory8 = new StatusHistory();
        statusHistory8.setChangeByName("Change By Name");
        statusHistory8.setChangeByUserId("42");
        statusHistory8.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory8.setId(1L);
        statusHistory8.setSittingRecord(new SittingRecord());
        statusHistory8.setStatusId(RECORDED);

        SittingRecord sittingRecord8 = new SittingRecord();
        sittingRecord8.addStatusHistory(statusHistory8);
        sittingRecord8.setAm(true);
        sittingRecord8.setContractTypeId(1L);
        sittingRecord8.setEpimmsId("42");
        sittingRecord8.setHmctsServiceId("42");
        sittingRecord8.setId(1L);
        sittingRecord8.setJudgeRoleTypeId("42");
        sittingRecord8.setPersonalCode("Personal Code");
        sittingRecord8.setPm(true);
        sittingRecord8.setRegionId("us-east-2");
        sittingRecord8.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord8.setStatusId(RECORDED);

        StatusHistory statusHistory9 = new StatusHistory();
        statusHistory9.setChangeByName("Change By Name");
        statusHistory9.setChangeByUserId("42");
        statusHistory9.setChangeDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        statusHistory9.setId(1L);
        statusHistory9.setSittingRecord(sittingRecord8);
        statusHistory9.setStatusId(RECORDED);

        SittingRecord sittingRecord9 = new SittingRecord();
        sittingRecord9.addStatusHistory(statusHistory9);
        sittingRecord9.setAm(true);
        sittingRecord9.setContractTypeId(1L);
        sittingRecord9.setEpimmsId("42");
        sittingRecord9.setHmctsServiceId("42");
        sittingRecord9.setId(1L);
        sittingRecord9.setJudgeRoleTypeId("42");
        sittingRecord9.setPersonalCode("Personal Code");
        sittingRecord9.setPm(true);
        sittingRecord9.setRegionId("us-east-2");
        sittingRecord9.setSittingDate(LocalDate.of(1970, 1, 1));
        sittingRecord9.setStatusId(RECORDED);
        sittingRecord.addStatusHistory(statusHistory);
        SittingRecord sittingRecord2 = createSittingRecord(true, 1L, "EP2", "HM2",
                                                           2L, "42", "PC1", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory2 = createStatusHistory("Matt Murdock", "11244",
                                                          LocalDateTime.now(),  2L, StatusId.RECORDED.name());
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

        SittingRecord sittingRecord4 = createSittingRecord(true, 1L, "42", "42",
                                                           4L, "42", "PC1", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory4 = createStatusHistory("Bruce Wayne", "11266",
                                                          LocalDateTime.now(),  4L, StatusId.RECORDED.name());
        sittingRecord4.addStatusHistory(statusHistory4);
        SittingRecord sittingRecord5 = createSittingRecord(true, 1L, "EP5", "HM5",
                                                           5L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory5 = createStatusHistory("Lois Lane", "11277",
                                                         LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           5L, StatusId.RECORDED.name());
        sittingRecord5.addStatusHistory(statusHistory5);
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
        SittingRecord sittingRecord9 = createSittingRecord(true, 1L, "42", "42",
                                                           9L, "JRT5", "PC5", true,
                                                           "us-east-2",
                                                           LocalDate.of(1970, 1, 1),
                                                           StatusId.RECORDED.name());
        StatusHistory statusHistory9 = createStatusHistory("Lois Lane", "11277",
                                                           LocalDate.of(1970, 1, 1).atStartOfDay(),
                                                           9L, StatusId.RECORDED.name());
        statusHistory9.setSittingRecord(sittingRecord9);

        statusHistoryService.saveStatusHistory(statusHistory9, sittingRecord9);
        verify(sittingRecordRepository).save(Mockito.any());
        verify(statusHistoryRepository).save(Mockito.any());
        SittingRecord sittingRecordRetrieved = statusHistory9.getSittingRecord();
        assertSame(sittingRecord9, sittingRecordRetrieved);
        assertEquals(2, sittingRecordRetrieved.getStatusHistories().size());
        assertEquals(StatusId.RECORDED.name(), sittingRecordRetrieved.getStatusId());
    }

    private StatusHistory createStatusHistory(String changedByName, String changedByUserId,
                                              LocalDateTime changedDateTime,
                                              Long id, String statusId) {

        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setChangedByName(changedByName);
        statusHistory.setChangedByUserId(changedByUserId);
        statusHistory.setChangedDateTime(changedDateTime);
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

