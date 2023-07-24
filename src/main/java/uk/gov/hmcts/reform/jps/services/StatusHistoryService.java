package uk.gov.hmcts.reform.jps.services;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;

import java.time.LocalDate;
import java.util.List;

public interface StatusHistoryService {

    List<StatusHistory> findAll();

    List<RecordingUser> findRecordingUsers(String hmctsServiceId,
                                                  String regionId,
                                                  List<String> statusIds,
                                                  LocalDate startDate,
                                                  LocalDate endDate);

    void saveStatusHistory(StatusHistory statusHistory, SittingRecord sittingRecord);

}
