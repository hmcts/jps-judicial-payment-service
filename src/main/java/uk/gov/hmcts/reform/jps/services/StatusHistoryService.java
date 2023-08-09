package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

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
}
