package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class StatusHistoryService {

    private final SittingRecordRepository sittingRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public List<StatusHistory> findAll() {
        return statusHistoryRepository.findAll();
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
        Sort.TypedSort<StatusHistory> sort = Sort.sort(StatusHistory.class);
        Optional<StatusHistory> lastStatusHistory = statusHistoryRepository.findFirstBySittingRecord(
            uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(sittingRecordDuplicateCheckFields.getId())
                .build(),
            sort.by(StatusHistory::getId).descending()
        );

        lastStatusHistory.ifPresent(lastStatusHistoryRecorded -> {
            sittingRecordWrapper.setCreatedByName(lastStatusHistoryRecorded.getChangeByName());
            sittingRecordWrapper.setCreatedDateTime(lastStatusHistoryRecorded.getChangeDateTime());
            sittingRecordWrapper.setStatusId(lastStatusHistoryRecorded.getStatusId());
        });
    }

    @Transactional
    public void insertRecord(Long sittingRecordId,
                             StatusId statusId,
                             String changedByUserId,
                             String changedByUserName) {
        StatusHistory statusHistory = StatusHistory.builder()
            .sittingRecord(SittingRecord.builder()
                               .id(sittingRecordId)
                               .build())
            .statusId(statusId)
            .changeByUserId(changedByUserId)
            .changeByName(changedByUserName)
            .changeDateTime(LocalDateTime.now())
            .build();
        statusHistoryRepository.save(statusHistory);
    }
}
