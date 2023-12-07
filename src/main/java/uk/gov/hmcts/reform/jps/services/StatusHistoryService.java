package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class StatusHistoryService {

    private final SittingRecordRepository sittingRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public List<StatusHistory> findAll() {
        return statusHistoryRepository.findAll();
    }

    public List<RecordingUser> findRecordingUsers(String hmctsServiceId,
                                           String regionId,
                                           List<StatusId> statusIds,
                                           LocalDate startDate,
                                           LocalDate endDate) {
        return statusHistoryRepository.findRecordingUsers(hmctsServiceId,
                                                   regionId,
                                                   statusIds,
                                                   startDate,
                                                   endDate);
    }

    @Transactional
    public void saveStatusHistory(StatusHistory statusHistory, SittingRecord sittingRecord) {
        sittingRecord.addStatusHistory(statusHistory);
        statusHistoryRepository.save(statusHistory);
        sittingRecordRepository.save(sittingRecord);
    }

    public void updateFromStatusHistory(SittingRecordWrapper sittingRecordWrapper,
                                        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                                                                            sittingRecordDuplicateCheckFields) {
        Optional<StatusHistory> firstStatusHistory = statusHistoryRepository.findBySittingRecordAndStatusId(
            uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(sittingRecordDuplicateCheckFields.getId())
                .build(),
            RECORDED
        );

        firstStatusHistory.ifPresent(firstStatusHistoryRecorded -> {
            sittingRecordWrapper.setCreatedByName(firstStatusHistoryRecorded.getChangedByName());
            sittingRecordWrapper.setCreatedDateTime(firstStatusHistoryRecorded.getChangedDateTime());
            sittingRecordWrapper.setStatusId(sittingRecordDuplicateCheckFields.getStatusId());
        });
    }

    @Transactional
    public void insertRecord(Long sittingRecordId,
                             StatusId statusId,
                             String changedByUserId,
                             String changedByUserName) {
        addStatusHistory(SittingRecord.builder()
                             .id(sittingRecordId)
                             .build(),
                         statusId,
                         changedByUserId,
                         changedByUserName);
    }

    @Transactional
    public void publish(SittingRecord sittingRecord,
                             String changedByUserId,
                             String changedByUserName) {
        addStatusHistory(sittingRecord,
                         StatusId.PUBLISHED,
                         changedByUserId,
                         changedByUserName);
    }

    private void addStatusHistory(SittingRecord sittingRecord,
                        StatusId statusId,
                        String changedByUserId,
                        String changedByUserName) {
        StatusHistory statusHistory = StatusHistory.builder()
            .sittingRecord(sittingRecord)
            .statusId(statusId)
            .changedByUserId(changedByUserId)
            .changedByName(changedByUserName)
            .changedDateTime(LocalDateTime.now())
            .build();
        statusHistoryRepository.save(statusHistory);
    }
}
