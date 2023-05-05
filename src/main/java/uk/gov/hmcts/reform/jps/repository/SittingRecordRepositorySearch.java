package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.util.List;

public interface SittingRecordRepositorySearch {
    List<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest,
                             String hmctsServiceCode);

    List<SittingRecord> findByUser(SittingRecordSearchRequest recordSearchRequest,
                             String hmctsServiceCode,
                             String userId);

    List<SittingRecord> findByIgnoreUserId(SittingRecordSearchRequest recordSearchRequest,
                             String hmctsServiceCode,
                             String userId);

    int recordCountByUser(SittingRecordSearchRequest recordSearchRequest,
                          String hmctsServiceCode,
                          String userId);

    int totalRecords(SittingRecordSearchRequest recordSearchRequest,
                          String hmctsServiceCode);
}


