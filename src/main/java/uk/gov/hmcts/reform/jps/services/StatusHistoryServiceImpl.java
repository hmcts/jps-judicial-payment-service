package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDate;
import java.util.List;
import javax.transaction.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class StatusHistoryServiceImpl implements StatusHistoryService {

    private final SittingRecordRepository sittingRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public List<StatusHistory> findAll() {
        return statusHistoryRepository.findAll();
    }

    public List<RecordingUser> findRecordingUsers(String hmctsServiceId,
                                           String regionId,
                                           List<String> statusIds,
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

}
